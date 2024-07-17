package com.jd.workflow.console.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.up.portal.login.interceptor.UpLoginContextHelper;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.Authorization;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.AuthorizationKeyTypeEnum;
import com.jd.workflow.console.base.enums.ParseType;
import com.jd.workflow.console.dto.CallHttpToWebServiceReqDTO;
import com.jd.workflow.console.dto.FilterParam;
import com.jd.workflow.console.dto.InvokeMethodDTO;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.dto.datasource.DataSourceInvokeDto;
import com.jd.workflow.console.dto.doc.ListModifyLogDto;
import com.jd.workflow.console.dto.doc.UpdateMethodConfigDto;
import com.jd.workflow.console.dto.doc.method.MethodDocConfig;
import com.jd.workflow.console.dto.version.CompareMethodVersionDTO;
import com.jd.workflow.console.entity.ColorUpdateParam;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.helper.WebServiceHelper;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.color.ColorApiParam;
import com.jd.workflow.console.service.color.ColorApiServiceImpl;
import com.jd.workflow.console.service.color.ColorApiSimple;
import com.jd.workflow.console.service.color.ColorCluster;
import com.jd.workflow.console.service.doc.IMethodModifyLogService;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import com.jd.workflow.console.service.method.MethodModifyDeltaInfoService;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.server.dto.InterfaceAndMethodInfo;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author wubaizhao1
 * @date: 2022/5/16 20:47
 */
@Slf4j
@RestController
@RequestMapping("/methodManage")
@UmpMonitor
@Api(value = "方法管理",tags="方法管理")
public class MethodManageController {

    /**
     * @date: 2022/5/16 20:49
     * @author wubaizhao1
     */
    @Autowired
    MethodManageServiceImpl methodManageService;

    @Resource
    ColorApiServiceImpl colorApiService;

    @Resource
    IMethodModifyLogService methodModifyLogService;

    @Autowired
    MethodModifyDeltaInfoService methodModifyDeltaInfoService;

    @Autowired
    private IMeasureDataService measureDataService;

    @Autowired
    private IAppInfoService appInfoService;


    /**
     * 分页查询
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/16 20:38
     * @author wubaizhao1
     */
    @GetMapping("/pageMethod")
    @ApiOperation(value = "查看方法")
    public CommonResult<Page<MethodManageDTO>> pageMethod(MethodManageDTO methodManageDTO) {
        log.info("MethodManageController pageMethod query={}", JsonUtils.toJSONString(methodManageDTO));
        //1.判空
        //2.入参封装
        //3.service层
        Page<MethodManageDTO> result = methodManageService.pageMethod(methodManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * 添加
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/16 20:40
     * @author wubaizhao1
     */
    @PostMapping("/add")
    @Authorization(key = "interfaceId", parseType = ParseType.BODY)
    @ApiOperation(value = "添加方法")
    public CommonResult<Long> add(@RequestBody MethodManageDTO methodManageDTO) {
        log.info("MethodManageController add query={}", JsonUtils.toJSONString(methodManageDTO));
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //3.service层
        Long ref = methodManageService.add(methodManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 前端未调用，维护数据
     * @param ff
     * @return
     */

    @PostMapping("/addColor")
    @ApiOperation(value = "添加color网关参数")
    private String addInfo(@RequestBody String ff){
        methodManageService.addColorInfo(ff);
        return null;
    }

    @GetMapping("/copy")
    @ApiOperation(value = "复制方法")
    public CommonResult<Long> copy(Integer methodId,Long groupId) {
        log.info("MethodManageController copy query={}", JsonUtils.toJSONString(methodId));
        //1.判空
        //2.入参封装
        //3.service层
        MethodManageDTO source = methodManageService.getEntity(methodId+"");
        source.setName(source.getName()+"Copy");
        if(StringUtils.isNotBlank(source.getMethodCode())){
            source.setMethodCode(source.getMethodCode()+"Copy");
        }else{
            String[] paths=source.getPath().split("/");
            source.setMethodCode(paths[paths.length-1]+"Copy");
        }
        source.setGroupId(groupId);
        Long ref =null;
        if(source.getType()==3){
            ref= methodManageService.copyJsfMethod(groupId,source);
        }else{
            ref = methodManageService.add(source);
        }
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 修改
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/16 20:39
     * @author wubaizhao1
     */
    @PostMapping("/edit")
   // @Authorization(key = "id", keyType = AuthorizationKeyTypeEnum.METHOD, parseType = ParseType.BODY)
    @ApiOperation(value = "修改方法")
    public CommonResult<Long> edit(@RequestBody MethodManageDTO methodManageDTO) {
        log.info("MethodManageController edit query={}", JsonUtils.toJSONString(methodManageDTO));
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //3.service层
        Long ref = methodManageService.edit(methodManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @GetMapping("removeDelta")
    public CommonResult<Boolean> removeDelta(Long methodId){
        methodModifyDeltaInfoService.removeDelta(methodId);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 删除
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/16 20:39
     * @author wubaizhao1
     */
    @PostMapping("/remove")
    @Authorization(key = "id", keyType = AuthorizationKeyTypeEnum.METHOD, parseType = ParseType.BODY)
    @ApiOperation(value = "删除方法")
    public CommonResult<Boolean> remove(@RequestBody MethodManageDTO methodManageDTO) {
        log.info("MethodManageController remove query={}", JsonUtils.toJSONString(methodManageDTO));
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //3.service层
        Boolean ref = methodManageService.remove(methodManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * 根据id获取详情
     *
     * @param id
     * @param filter 2 color客户端必传信息
     * @return
     * @date: 2022/5/24 14:24
     * @author wubaizhao1
     */
    @GetMapping("/getById")
    @ApiOperation(value = "获取方法详情")
    public CommonResult<MethodManageDTO> getById(String id,FilterParam filter) {
        log.info("MethodManageController getById id={}", JsonUtils.toJSONString(id));
        //入参封装
        //String operator=UserSessionLocal.getUser().getUserId();
        //service层
        MethodManageDTO ref = methodManageService.getMethodManageDTOById(id,filter);
        //补下markdown数据
//        MethodManageDTO ref = methodManageService.getEntity(id);
        // 【指标度量】接口详情
        measureDataService.saveInterfaceDetailLog(id);
        //出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/updateContent")
    @ApiOperation(value = "测试-更新content")
    public CommonResult<Boolean> updateContent(@RequestBody Map<String,String> content) {
        methodManageService.updateObject(Long.valueOf(content.get("id")),content.get("content"));
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/updateSyncStatus")
    @ApiOperation(value = "更新方法的同步状态")
    public CommonResult<Long> updateSyncStatus(Long id, Integer reportSyncStatus) {
        log.info("MethodManageController updateSyncStatus id={},reportSyncStatus={}", id, reportSyncStatus);
        //出参
        return CommonResult.buildSuccessResult(methodManageService.updateReportStatus(id, reportSyncStatus));
    }
    @GetMapping("/updateStatus")
    @ApiOperation(value = "更新接口状态")
    public CommonResult<Boolean> updateStatus(Long id, Integer status) {
        log.info("MethodManageController updateSyncStatus id={},reportSyncStatus={}", id, status);
        //出参
        return CommonResult.buildSuccessResult(methodManageService.updateStatus(id, status));
    }

    /**
     * color侧调用，更新functionId信息
     * @return
     */
    @PostMapping("/updateFunctionId")
    @ApiOperation(value = "更新funcitonId")
    public CommonResult<Boolean> updateFunctionId(@RequestBody ColorUpdateParam param) {
        log.info("MethodManageController updateFunctionId param={}", JSONObject.toJSONString(param) );
        Guard.notEmpty(param.getDocUrl(),"docUrl不可为空");
        Guard.notEmpty(param.getZone(),"zone不可为空");
        Guard.notEmpty(param.getFunctionId(),"functionId不可为空");
        String methodId = null;
        String docUrl=param.getDocUrl();
        if (docUrl.contains("paas.jd.com")) {
            methodId= methodManageService.getMethodIdByDoc(docUrl,"methodId");
        }
        if (docUrl.contains("j-api.jd.com")) {
            methodId= methodManageService.getMethodIdByDoc(docUrl,"apiID");
        }
        if(StringUtils.isNotEmpty(methodId)){
            Boolean res = methodManageService.updateFunctionId(Long.valueOf(methodId),param.getZone(),param.getFunctionId(),param.getType());
            return CommonResult.buildSuccessResult(res);
        }else{
            return CommonResult.buildErrorCodeMsg(999,"methodId为空");
        }
    }


    /**
     * 更新webservice方法列表
     *
     * @param methodManageDTO
     * @return
     * @date: 2022/5/24 16:08
     * @author wubaizhao1
     */
    @PostMapping("/updateWebService")
    @Authorization(key = "interfaceId", keyType = AuthorizationKeyTypeEnum.INTERFACE, parseType = ParseType.BODY)
    @ApiOperation(value = "刷新webservice 方法列表")
    public CommonResult<Boolean> updateWebService(@RequestBody MethodManageDTO methodManageDTO) throws Exception {
        log.info("MethodManageController updateWebService query={}", JsonUtils.toJSONString(methodManageDTO));
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        //3.service层
        Boolean ref = null;
        ref = methodManageService.updateWebService(methodManageDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }


    /**
     * @param invokeMethodDTO
     * @return
     * @date: 2022/5/19 17:33
     * @author wubaizhao1
     */
    @PostMapping("/invokeMethod")
    @Authorization(key = "id", keyType = AuthorizationKeyTypeEnum.METHOD, parseType = ParseType.BODY)
    public CommonResult<Object> invokeMethod(@RequestBody InvokeMethodDTO invokeMethodDTO) {
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        log.info("MethodManageController invokeMethod invokeMethodDTO={}", invokeMethodDTO);
        //3.service层
        Object ref = methodManageService.invokeMethod(invokeMethodDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/invokeDataSourceMethod")
    @Authorization(key = "id", keyType = AuthorizationKeyTypeEnum.INTERFACE, parseType = ParseType.BODY)
    @ApiOperation(value = "调试ducc")
    public CommonResult<Object> invokeDataSourceMethod(@RequestBody DataSourceInvokeDto invokeMethodDTO) {
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        log.info("MethodManageController invokeMethod invokeMethodDTO={}", invokeMethodDTO);
        //3.service层
        Object ref = methodManageService.invokeDataSourceMethod(invokeMethodDTO);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    /**
     * @param
     * @return
     * @date: 2022/5/19 17:33
     * @author wubaizhao1
     */
    @PostMapping("/invokeWebService")
    @Authorization(key = "id", keyType = AuthorizationKeyTypeEnum.METHOD, parseType = ParseType.BODY)
    @ApiOperation(value = "webService接口调试")
    public CommonResult<HttpOutput> invokeWebService(@RequestBody CallHttpToWebServiceReqDTO dto, HttpServletRequest request) {
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        log.info("MethodManageController invokeMethod invokeMethodDTO={}", JsonUtils.toJSONString(dto));
        String basePath = WebServiceHelper.getBasePath(request);
        String cookie = request.getHeader("Cookie");
        //3.service层
        HttpOutput output = methodManageService.invokeWebService(basePath, cookie, dto);
        //4.出参
        return CommonResult.buildSuccessResult(output);
    }

    @GetMapping("/checkWsdlPath")
    public CommonResult<Boolean> checkWsdlPath(String path) {
        //1.判空
        //2.入参封装
        String operator = UserSessionLocal.getUser().getUserId();
        log.info("MethodManageController checkWsdlPath query={}", path);
        //3.service层
        Boolean ref = methodManageService.checkWsdlPath(path);
        //4.出参
        return CommonResult.buildSuccessResult(ref);
    }

    @PostMapping("/updateDocConfig")
    @Authorization(key = "methodId", keyType = AuthorizationKeyTypeEnum.METHOD, parseType = ParseType.BODY)
    @ApiOperation(value = "修改方法描述")
    public CommonResult<MethodDocConfig> updateDocConfig(@RequestBody @Valid UpdateMethodConfigDto dto) {
        //4.出参
        return CommonResult.buildSuccessResult(methodManageService.updateDocConfig(dto));
    }

    @PostMapping("/modifyLogList")
    @ApiOperation(value = "方法修改记录")
    public CommonResult<IPage<MethodModifyLog>> modifyLogList(@RequestBody @Valid ListModifyLogDto dto) {
        Guard.notNull(dto.getInterfaceId(), "方法修改日志列表查询入参interfaceId不能为空");
        Guard.notNull(dto.getInterfaceId(), "方法修改日志列表查询入参methodId不能为空");
        return CommonResult.buildSuccessResult(methodModifyLogService.listModifyLogs(dto));
    }

    @PostMapping("/updateLogRemark")
    @ApiOperation(value = "更改日志备注")
    public CommonResult<Boolean> updateLogRemark(Long id,String remark) {
        Guard.notNull(id, "方法修改日志列表查询入参id不能为空");
        return CommonResult.buildSuccessResult(methodModifyLogService.updateLogRemark(id,remark));
    }

    @GetMapping("/modifyLogDetail")
    @ApiOperation(value = "方法修改记录详情")
    public CommonResult<MethodModifyLog> modifyLogDetail(Long id) {
        Guard.notNull(id, "方法修改日志详情查询入参id不能为空");
        MethodModifyLog methodModifyLog = methodModifyLogService.getDetailById(id);
        return CommonResult.buildSuccessResult(methodModifyLog);
    }

    @GetMapping(value = "/compareMethod")
    @ResponseBody
    public CommonResult<CompareMethodVersionDTO> listModifyLogs(Long id) {
        return CommonResult.buildSuccessResult(methodModifyLogService.compareMethod(id));
    }

    /**
     * @hidden
     * @param ids
     * @param interfaceId
     * @return
     */
    @PostMapping(value = "/updateMethodInterfaceId")
    @ResponseBody
    public CommonResult<Boolean> listModifyLogs(@RequestBody  List<Long> ids,Long interfaceId) {
        Guard.notEmpty(ids,"ids不能为空");
        Guard.notEmpty(interfaceId,"interfaceId不能为空");
        methodManageService.updateMethodInterfaceId(ids,interfaceId);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * @hidden
     * @return
     */
    @GetMapping(value = "/updateMergedDigest")
    @ResponseBody
    public CommonResult<Boolean> updateMergedDigest() {

        methodManageService.updateMergedDigest();
        return CommonResult.buildSuccessResult(true);
    }


    @GetMapping("/exportInterface")
    @ApiOperation(value = "导出接口列表")
    public CommonResult<Boolean> exportInterface(@RequestBody MethodManageDTO methodManageDTO) {
        log.info("MethodManageController pageMethod query={}", JsonUtils.toJSONString(methodManageDTO));
        //1.判空
        //2.入参封装
        //3.service层
        Page<MethodManageDTO> result = methodManageService.pageMethod(methodManageDTO);
        List<MethodManageDTO> records = result.getRecords();
        methodManageService.exportInterface(records);
        return CommonResult.buildSuccessResult(true);
    }
    /**
     * 查询接口和方法信息
     * @param type 1:http 3:jsf
     * @param appCode 应用编码
     * @param current 当前页
     *                @param size 每页大小
     * @return
     */
    @GetMapping("/queryInterfaceAndMethodInfo")
    public CommonResult<IPage<InterfaceAndMethodInfo>> queryInterfaceAndMethodInfo(Integer type, String appCode, Long current, Long size
    ) {


        return CommonResult.buildSuccessResult(methodManageService.queryInterfaceAndMethodInfo(type,appCode,current,size));
    }




    /**
     * 检索functionId
     * @return
     */
    @GetMapping(value = "/queryColorInfo")
    @ResponseBody
    public CommonResult<List<ColorApiParam>> queryColorInfo(String functionId, String zoneCluster) {
        Guard.notEmpty(functionId,"functionId不能为空");
        List<ColorApiParam> data= null;
        try {
            data = colorApiService.queryColorInfoByFunctionId(functionId, UpLoginContextHelper.getUserPin(),zoneCluster);
        } catch (Exception e) {
            return CommonResult.buildErrorCodeMsg(999,e.getMessage());
        }
        return CommonResult.buildSuccessResult(data);
    }

    /**
     *
     * @param functionId
     * @param env prod或者beta
     * @return
     */
    @GetMapping(value = "/queryApiClusterList")
    @ResponseBody
    public CommonResult<List<ColorCluster>> apiClusterList(String functionId,String env) {
        Guard.notEmpty(functionId,"functionId不能为空");
        Guard.notEmpty(env,"env不能为空");
        List<ColorCluster> data= null;
        try {
            data= colorApiService.queryApiClusterListByFunctionId(functionId,env);
        } catch (Exception e) {
            return CommonResult.buildErrorCodeMsg(999,e.getMessage());
        }
        return CommonResult.buildSuccessResult(data);
    }

    @GetMapping(value = "/queryAllApiCluster")
    @ResponseBody
    public CommonResult<Map<String,List<ColorCluster>>> queryAllApiCluster(String functionId) {
        Guard.notEmpty(functionId,"functionId不能为空");
        List<ColorCluster> prodData= new ArrayList<>();
        List<ColorCluster> betaData= new ArrayList<>();
        Map<String,List<ColorCluster>> data=new HashMap<>();
        try {
            prodData= colorApiService.queryApiClusterListByFunctionId(functionId,"prod");
            betaData= colorApiService.queryApiClusterListByFunctionId(functionId,"beta");
            data.put("prod",prodData);
            data.put("beta",betaData);
        } catch (Exception e) {
            return CommonResult.buildErrorCodeMsg(999,e.getMessage());
        }
        return CommonResult.buildSuccessResult(data);
    }


    /**
     * 模糊查询 functionId 查询域名
     * @param functionId
     * @return
     */
    @GetMapping(value = "/fuzzySearch")
    @ResponseBody
    public CommonResult<List<ColorApiSimple>> isColorApiMember(String functionId) {
        Guard.notEmpty(functionId, "functionId");
        List<ColorApiSimple> data = new ArrayList<>();
        try {
            data = colorApiService.fuzzySearch(functionId);
        } catch (Exception e) {
            log.error("Fuzzy search异常", e);
        }
        return CommonResult.buildSuccessResult(data);
    }

}
