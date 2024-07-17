package com.jd.workflow.console.controller;
import java.awt.*;
import java.time.LocalDateTime;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jayway.jsonpath.JsonPath;
import com.jd.cjg.RpcResponse;
import com.jd.cjg.result.CjgResult;
import com.jd.cjg.unapp.UnAppOpenProvider;
import com.jd.cjg.unapp.UnAppProvider;
import com.jd.cjg.unapp.request.AppBaseQueryReq;
import com.jd.cjg.unapp.request.GitAppReq;
import com.jd.cjg.unapp.vo.AppInfoVo;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.Menu;
import com.jd.workflow.console.entity.doc.SyncJsfDocLog;
import com.jd.workflow.console.jme.JdMEAccessTokenRequest;
import com.jd.workflow.console.service.DevCodeService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.WebHookServiceImpl;
import com.jd.workflow.console.service.app.UnAppProviderWarp;
import com.jd.workflow.console.service.color.ColorApiParam;
import com.jd.workflow.console.service.doc.SyncJsfDocLogService;
import com.jd.workflow.console.service.ducc.DuccConfigServiceAdapter;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.console.service.remote.api.JagileService;
import com.jd.workflow.console.service.remote.api.dto.jagile.JagileMember;
import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;
import com.jd.workflow.console.utils.UUIDUtil;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.webhook.Project;
import com.jd.workflow.webhook.WebHookVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequestMapping("/webhook")
@UmpMonitor
public class WebHookController {

    @Resource
    private UnAppOpenProvider unAppOpenProvider;

    @Autowired
    private UnAppProviderWarp unAppProviderWarp;

    @Resource
    private AppInfoServiceImpl appInfoService;

    @Resource
    private IInterfaceManageService interfaceManageService;

    @Autowired
    private SyncJsfDocLogService syncJsfDocLogService;

    @Resource
    DevCodeService devCodeService;

    @Resource
    WebHookServiceImpl webHookService;




    @PostMapping("/jagline")
    @ResponseBody
    public CommonResult<Object> jagline(HttpServletResponse response, HttpServletRequest request, @RequestBody WebHookVo param) {

        if (StringUtils.isBlank(param.getFlowId())) {
            param.setFlowId(UUIDUtil.getUUID());
        }
        log.info("jagline:param={}", JSONObject.toJSONString(param));
        try {
            JSONObject rest = webHookService.jagline(param);
            return CommonResult.buildSuccessResult(rest);
        } catch (Exception e) {
            return CommonResult.buildErrorCodeMsg(999, e.getMessage());
        }

    }


    /**
     * 1、通过jdos应用code查询coding地址
     * 2、通过coding地址查询应用集合
     * 3、应用下是否有上报的接口数据 有不出里，没有调用上报
     *
     * @param appCodeStr
     * @return
     */
    @GetMapping("/batchExe")
    @ResponseBody
    public CommonResult<Object> batchExe(String appCodeStr,String path,Integer type,String flowId) {
        if (StringUtils.isNotBlank(appCodeStr)) {
            final String[] appCodes = appCodeStr.split(",");
            if (appCodes != null) {
                for (String appCode : appCodes) {
                    SyncJsfDocLog syncJsfDocLog = new SyncJsfDocLog();
                    if(StringUtils.isBlank(flowId)){
                        flowId=UUID.randomUUID().toString();
                    }
                    syncJsfDocLog.setFlowId(flowId);
                    syncJsfDocLog.setInterfaceName("");
                    syncJsfDocLog.setAppCode(appCode);
                    syncJsfDocLog.setYn(0);
                    if(null==type){
                        syncJsfDocLog.setType(0);
                    }else{
                        syncJsfDocLog.setType(type);
                    }
                    syncJsfDocLog.setFlowType(0);
                    try {
                        CommonResult<Object> result = requestApp(appCode,null,flowId,type);
                        syncJsfDocLog.setStatus(21);
                        syncJsfDocLog.setCodePath(path);
                        JSONObject data=null;
                        if(null!=result&&null!=result.getData()){
                            if(result.getData() instanceof JSONObject){
                                data=JSONObject.parseObject(JSONObject.toJSONString(result.getData()));
                                if(null!=data&&null!=data.getJSONObject("data")){
                                    syncJsfDocLog.setBuildUrl(data.getJSONObject("data").getString("build_detail_url"));
                                }
                            }
                        }
                        syncJsfDocLog.setRemart(JSON.toJSONString(result));
                    } catch (Throwable e) {
                        log.error("WebHookController.batchExe Exception ",e);
                        syncJsfDocLog.setStatus(22);
                        syncJsfDocLog.setRemart(e.getMessage());
                    }finally {
                        syncJsfDocLogService.save(syncJsfDocLog);
                    }
                }
            }
        }
        return CommonResult.buildSuccessResult(true);
    }


    @GetMapping("/insertResult")
    @ResponseBody
    public CommonResult<Boolean> insertResult(SyncJsfDocLog docLog){
        log.info("insertResult 入参 appCode={},result={},status={},",docLog.getAppCode(),docLog.getResultUrl(),docLog.getRemart());
         webHookService.insertResult(docLog);
        return CommonResult.buildSuccessResult(true);
    }

    private CommonResult<Object> requestApp(String appCode,String compilePath,String flowId,Integer type) {
        CommonResult<Object> result = null;
        AppInfoVo appInfoVo = unAppProviderWarp.obtainAppInfoVo(appCode);
        if (Objects.nonNull(appInfoVo)) {
            String codeAddress = appInfoVo.getCodeAddress();
            if(StringUtils.isNotBlank(codeAddress)){
//                GitAppReq req = new GitAppReq();
//                req.setGit(codeAddress);
//                RpcResponse<List<AppInfoVo>> vos = unAppOpenProvider.codingApps(req);
//                List<String> jApiAppCodes = vos.getData().stream().map(appInfoVo -> {
//                            return "J-dos-" + appInfoVo.getAppAlias();
//                        }
//                ).collect(Collectors.toList());
//                List<AppInfo> appInfoList = appInfoService.queryAppInfoListByAppCoeds(jApiAppCodes);
//                List<Long> appIds = appInfoList.stream().map(AppInfo::getId).collect(Collectors.toList());
//                List<InterfaceManage> appInterfaceByAppIdList = interfaceManageService.getAppInterfaceByAppIdList(appIds);
//                if (CollectionUtils.isEmpty(appInterfaceByAppIdList)) {
                    WebHookVo webHookVo = new WebHookVo();
                    Project project = new Project();
                    project.setGit_http_url(codeAddress);
                    project.setDefault_branch(appInfoVo.getBranch());
//                    webHookVo.setAppCode(appCode);
                    webHookVo.setFlowId(flowId);
                    webHookVo.setType(type);
                    if(StringUtils.isNotBlank(compilePath)){
                        webHookVo.setCompilePath(compilePath);
                    }
                    webHookVo.setProject(project);
                    result = jagline(null, null, webHookVo);
//                } else {
//                    result = CommonResult.buildSuccessResult("已通过JAPI插件上报");
//                }
            }else{
                throw new BizException("未找到coding地址");
            }
        }else{
            throw new BizException("未找到应用信息");
        }
        return result;
    }


    /**
     * 测试初始化devcode信息
     * @param info
     * @return
     */
    @PostMapping(value = "/initDevCodeInfo")
    @ResponseBody
    public CommonResult<String> initDevCodeInfo(@RequestBody DevCodeInfo info) {
        LambdaQueryWrapper<DevCodeInfo> wrapper=new LambdaQueryWrapper();
        wrapper.eq(DevCodeInfo::getName,info.getName());
        wrapper.eq(DevCodeInfo::getCodePath,info.getCodePath());
        wrapper.eq(DevCodeInfo::getCompilePath,info.getCompilePath());
        wrapper.eq(DevCodeInfo::getCompileParam,info.getCompileParam());
        List<DevCodeInfo> infos=devCodeService.list(wrapper);
        if(!CollectionUtils.isEmpty(infos)){
            return CommonResult.buildSuccessResult(infos.get(0).getId()+"");
        }
        if(info.getCompileCommand().length()>100){
            info.setCompileCommand(info.getCompileCommand().substring(0,100));
        }
        info.setYn(1);
        info.setCreated(new Date());
        info.setCreator("initInfo");
        devCodeService.save(info);
        return CommonResult.buildSuccessResult(info.getId()+"");
    }

    @GetMapping(value = "/getDev")
    @ResponseBody
    public CommonResult<List<Long>> getDev(String name,String path) {
        LambdaQueryWrapper<DevCodeInfo> wr=new LambdaQueryWrapper<>();
        wr.eq(DevCodeInfo::getName,name);
        wr.eq(DevCodeInfo::getCodePath,path);
        List<DevCodeInfo> infos=devCodeService.list(wr);
        return CommonResult.buildSuccessResult(infos.stream().map(DevCodeInfo::getId).collect(Collectors.toList()));
    }

    @GetMapping(value = "/remove")
    @ResponseBody
    public CommonResult<String> remove() {
        LambdaQueryWrapper<DevCodeInfo> wr=new LambdaQueryWrapper<>();
        wr.eq(DevCodeInfo::getYn,1);
        devCodeService.removeByIds(devCodeService.list().stream().map(DevCodeInfo::getId).collect(Collectors.toList()));
        return CommonResult.buildSuccessResult(null);
    }

    @GetMapping(value = "/updateParam")
    @ResponseBody
    public CommonResult<Boolean> updateParam(Long id,String branch,String pomPath,String profile) {
        Guard.notEmpty(id,"id不能为空");
        Guard.assertTrue(StringUtils.isEmpty(pomPath)||!pomPath.contains("#"),"修改pomPath");
        LambdaUpdateWrapper<DevCodeInfo> wr=new LambdaUpdateWrapper<>();
        wr.eq(DevCodeInfo::getYn,1);
        wr.eq(DevCodeInfo::getId,id);
        wr.set(StringUtils.isNotEmpty(branch),DevCodeInfo::getBranch,branch);
        wr.set(StringUtils.isNotEmpty(pomPath)&&!"cls".equals(pomPath),DevCodeInfo::getCompilePath,pomPath);
        wr.set(StringUtils.isNotEmpty(pomPath)&&"cls".equals(pomPath),DevCodeInfo::getCompilePath,"");
        wr.set(StringUtils.isNotEmpty(profile)&&!"cls".equals(profile),DevCodeInfo::getCompileParam,profile);
        wr.set(StringUtils.isNotEmpty(profile)&&"cls".equals(profile),DevCodeInfo::getCompileParam,"");
        return CommonResult.buildSuccessResult(devCodeService.update(wr));
    }

    @GetMapping(value = "/obtainAllFailData")
    @ResponseBody
    public CommonResult<List<String>> obtainAllFailData(String dateStr) {
        //获取失败记录
        LambdaQueryWrapper<SyncJsfDocLog> lqw = new LambdaQueryWrapper();
        Date date = DateUtil.parseDate(dateStr);
        lqw.gt(SyncJsfDocLog::getCreated,date);
        int status = 13;
        lqw.eq(SyncJsfDocLog::getStatus, status);
        List<SyncJsfDocLog> dataInfoEs = syncJsfDocLogService.list(lqw);
        List<Long> ids = dataInfoEs.stream().map(data -> {
            return data.getId() + 1;
        }).collect(Collectors.toList());
        //获取最终结果
        List<SyncJsfDocLog> resultList = syncJsfDocLogService.listByIds(ids);
        List<String> ret = resultList.stream().map(SyncJsfDocLog::getInterfaceName).collect(Collectors.toList());
        return CommonResult.buildSuccessResult(ret);
    }
    @GetMapping(value = "/obtainAllFail")
    @ResponseBody
    public CommonResult<Map<String, String>> obtainAllFail(String dateStr) {
        //获取失败记录
        LambdaQueryWrapper<SyncJsfDocLog> lqw = new LambdaQueryWrapper();
        Date date = DateUtil.parseDate(dateStr);
        lqw.gt(SyncJsfDocLog::getCreated,date);
        int status = 2;
        lqw.eq(SyncJsfDocLog::getStatus, status);
        List<SyncJsfDocLog> dataInfoEs = syncJsfDocLogService.list(lqw);
        Map<String, String> retMap = new HashMap<>();
        for (SyncJsfDocLog syncJsfDocLog : dataInfoEs) {
            try {
                if(StringUtils.isNotBlank(syncJsfDocLog.getAppCode())){
                    retMap.put(syncJsfDocLog.getAppCode(), syncJsfDocLog.getBuildUrl());
                }
            } catch (Exception e) {
                log.error("WebHookController.obtainAllFail Exception ",e );
            }
        }
        return CommonResult.buildSuccessResult(retMap);
    }



}
