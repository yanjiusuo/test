package com.jd.workflow.console.service.plugin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.enums.LoginTypeEnum;
import com.jd.workflow.console.dao.mapper.plugin.HotswapDeployInfoMapper;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.dto.plugin.HotDeployDto;
import com.jd.workflow.console.dto.test.deeptest.TestResult;
import com.jd.workflow.console.entity.plugin.HotswapDeployInfo;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.console.service.ducc.entity.HotUpdateEnvironmentConf;
import com.jd.workflow.console.service.plugin.log.SingleDeployInfo;
import com.jd.workflow.console.utils.JfsUtils;
import com.jd.workflow.console.utils.RestTemplateUtils;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import java.lang.reflect.Type;
import java.net.ConnectException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HotswapDeployInfoService extends ServiceImpl<HotswapDeployInfoMapper, HotswapDeployInfo> {

    public static String HOT_DEPLOY_REMOTE_FORMAT = "http://%s:55455";
    public static String HOT_DEPLOY_AGENT_FORMAT = "http://%s:55455/hot-swap/deploy";

    public static String HOT_DEPLOY_LOCAL_FORMAT = "http://%s/plugin/deployLocal";


    @Autowired
    HotUpdateService hotUpdateService;
    @Autowired
    IUserInfoService userInfoService;

    @Autowired
    private RestTemplateUtils restTemplateUtils;
@Autowired
    JfsUtils jfsUtils;

    /**
     *
     */
    @Value("${ducc.blackEnvs:xxx}")
    private String blackEnvs;

    public Long addHotDeploy(HotDeployDto dto) {
        Guard.notEmpty(dto.getReqId(), "reqId不可为空");
        HotswapDeployInfo deployInfo = new HotswapDeployInfo();
        BeanUtils.copyProperties(dto, deployInfo);
        deployInfo.setYn(1);
        if(deployInfo.getDeployFileStatisticInfo() != null){
            deployInfo.getDeployFileStatisticInfo().setOnlyUpdateClass(dto.isOnlyUpdateClass());
        }
        save(deployInfo);
        return deployInfo.getId();
    }

    public Long updateDeployInfo(HotDeployDto dto) {
        Guard.notEmpty(dto.getReqId(), "reqId不可为空");
        HotswapDeployInfo reqInfo = getByReqId(dto.getReqId());
        if (reqInfo == null) return null;
        BeanUtils.copyProperties(dto, reqInfo);
        updateById(reqInfo);
        return reqInfo.getId();
    }

    public HotswapDeployInfo getByReqId(String reqId) {
        LambdaQueryWrapper<HotswapDeployInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(HotswapDeployInfo::getYn, 1);
        lqw.eq(HotswapDeployInfo::getReqId, reqId);
        List<HotswapDeployInfo> list = list(lqw);
        if (list.isEmpty()) return null;
        return list.get(0);
    }

    /**
     * 远程发布
     *
     * @param dto
     * @return
     */
    public CommonResult<String> remoteHotDeploy(HotDeployDto dto) {
        CommonResult<String> result = null;
        long start = System.currentTimeMillis();


        //1.记录发布日志。
        Long hotLogId = addHotDeploy(dto);
        List<String> envCodes = Arrays.stream(blackEnvs.split(",")).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(envCodes) && envCodes.contains(dto.getEnvCode())) {
            result = new CommonResult<>();
            result.setCode(401);
            result.setData("当前环境已经封板");
            result.setMessage("当前环境已经封板");
            return result;
        }
        boolean isEone = false;
        if("eoneLane".equals(dto.getEnvCode())){ // eoneLane算测试环境
            dto.setEnvCode("test");
            isEone = true;
        }



        //2.根据环境，查询转发地址
        HotUpdateEnvironmentConf hotUpdateEnvironmentConf = hotUpdateService.getEnvByCode(dto.getEnvCode());
        String localIp = "";


        //3.转发，并返回结果
        if (StringUtils.isEmpty(hotUpdateEnvironmentConf.getHost())) {
            result = localHotDeploy(dto);
        } else {
            if (StringUtils.isNotEmpty(hotUpdateEnvironmentConf.getBucketName())) {
                dto.setBucketName(hotUpdateEnvironmentConf.getBucketName());
            }
            HttpHeaders headers = new HttpHeaders();
            headers.add("Host", hotUpdateEnvironmentConf.getHost());
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = String.format(HOT_DEPLOY_LOCAL_FORMAT, hotUpdateEnvironmentConf.getHostIp());
            log.info("remoteHotDeploy url:{},header:{},body:{}", url, JSON.toJSONString(headers), JSON.toJSONString(dto));
            String response = restTemplateUtils.post(url, headers, JSON.toJSONString(dto));
            log.info("remoteHotDeploy  url:{}, body:{}, result:{}", url, JSON.toJSONString(dto), response);
            result = JsonUtils.parse(response, new TypeReference<CommonResult<String>>() {
            });

        }


        try {

            if (result.getCode().equals(0)) {
                dto.setSucceed(1);

            } else {
                dto.setSucceed(0);
            }
        } catch (Exception ex) {
            log.error("remoteHotDeploy error result:" + result, ex);
        }

        dto.setRemoteDeployCostTime(Variant.valueOf(System.currentTimeMillis() - start).toInt());
        if (Objects.nonNull(dto.getCompileCostTime()) && Objects.nonNull(dto.getRemoteDeployCostTime())) {
            dto.setDeployTotalCostTime(dto.getCompileCostTime() + dto.getRemoteDeployCostTime());
        }
        if(isEone){
            dto.setEnvCode(null);
        }
        updateDeployInfo(dto);

        return result;
    }

    public CommonResult<String> localHotDeploy(HotDeployDto dto) {
        String url = String.format(HOT_DEPLOY_AGENT_FORMAT, dto.getRemoteIp());
        try{
            String result = restTemplateUtils.postJson(url, JSON.toJSONString(dto.getHotFiles()));
            log.info("remoteHotDeploy   body:{}, result:{}", JSON.toJSONString(dto), result);
            return JsonUtils.parse(result, new TypeReference<CommonResult<String>>() {
            });
        }catch (Exception e){
            if(isConnectionException(e)){
                return CommonResult.buildErrorCodeMsg(9999,"连接目标服务器超时，请确保"+dto.getRemoteIp()+"容器开启了热更新服务(热更新服务端口为55455), 请参考https://joyspace.jd.com/pages/WFCrMitJHzJcEKpo5cJy QA 10.2 自助排查下。");
            }
            log.error("hotdeploy.err_deploy:dtos={}",dto,e);
            return CommonResult.buildErrorCodeMsg(9999,"未知错误");
        }

    }
    private boolean isConnectionException(Throwable e){
        if(e == null) return false;
        if(e instanceof ConnectException){
            return true;
        }
        return isConnectionException(e.getCause());
    }
    public CommonResult<SingleDeployInfo> queryLog(String envCode, String remoteIp, String reqId) {
        if("eoneLane".equals(envCode)){
            envCode = "test";
        }
        HotUpdateEnvironmentConf hotUpdateEnvironmentConf = hotUpdateService.getEnvByCode(envCode);
        if (StringUtils.isEmpty(hotUpdateEnvironmentConf.getHost())) {
            return CommonResult.buildSuccessResult(queryLocalLog(envCode, remoteIp, reqId));
        }
        //3.转发，并返回结果

        String queryLogIp = String.format("http://%s/plugin", hotUpdateEnvironmentConf.getHostIp());
        Map<String, Object> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Host", hotUpdateEnvironmentConf.getHost());

        RequestClient client = new RequestClient(queryLogIp, defaultHeaders);
        Map<String, Object> params = new HashMap<>();
        params.put("envCode", envCode);
        params.put("remoteIp", remoteIp);
        params.put("reqId", reqId);

        log.info("queryLog url:{},param:{}", queryLogIp, JSON.toJSONString(params));
        String result = client.get("/queryLocalLog", params);
        log.info("queryLog url:{},param:{},result:{}", queryLogIp, JSON.toJSONString(params), result);
        CommonResult<SingleDeployInfo> commonResult = JsonUtils.parse(result, new TypeReference<CommonResult<SingleDeployInfo>>() {
        });
        if (commonResult.getData() != null && commonResult.getData().getEnd() != null) {
            updateStatistic(commonResult.getData());
        }
        return commonResult;
    }

    public void saveErrorLog(String content,String fileName ){
        String response = jfsUtils.uploadToJss(content,"lht",fileName);
        log.info("jsf.success_update_content:response={}",response);
    }

    public SingleDeployInfo queryLocalLog(String envCode, String remoteIp, String reqId) {


        RequestClient requestClient = new RequestClient(String.format(HOT_DEPLOY_REMOTE_FORMAT, remoteIp), null);
        log.info("queryLocalLog url:{}", String.format(HOT_DEPLOY_REMOTE_FORMAT, remoteIp));
        String s = requestClient.get("/hot-swap/queryLog", null);
        log.info("queryLocalLog url:{},result:{}", String.format(HOT_DEPLOY_REMOTE_FORMAT, remoteIp), s);
        if(StringUtils.isEmpty(s)){
            return null;
        }
        CommonResult<SingleDeployInfo> deployResult = JsonUtils.parse(s, new TypeReference<CommonResult<SingleDeployInfo>>() {
        });
        SingleDeployInfo data = deployResult.getData();
        if (data != null) {
            if (data.getEnd() != null) {
                updateStatistic(data);
            }
        }
        return data;
    }

    private void updateStatistic(SingleDeployInfo data) {
        HotswapDeployInfo deployInfo = getByReqId(data.getReqId());
        if (deployInfo == null) return;
        deployInfo.setRemoteDeployCostTime((int) (data.getEnd() - data.getStart()));
        deployInfo.getDeployFileStatisticInfo().setRemoteStart(data.getStart());
        deployInfo.getDeployFileStatisticInfo().setRemoteEnd(data.getEnd());
        deployInfo.getDeployFileStatisticInfo().setFiledownloadTime(data.getFileDownloadTime());
        updateById(deployInfo);
    }

    public void initUser() {
        QueryWrapper<HotswapDeployInfo> lqw = new QueryWrapper<>();
        lqw.select("distinct deploy_erp");
        List<HotswapDeployInfo> infos = list(lqw);
        for (HotswapDeployInfo info : infos) {
            String erp = info.getDeployErp();
            UserInfoDTO dto = new UserInfoDTO();
            dto.setUserCode(erp);
            dto.setLoginType(LoginTypeEnum.ERP.getCode());
            userInfoService.checkAndAdd(dto);
        }
    }
}
