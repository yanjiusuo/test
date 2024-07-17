package com.jd.workflow.console.controller.test;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.jd.cjg.bus.BusInterfaceRpcService;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.base.enums.*;
import com.jd.workflow.console.config.dao.MetaContextHelper;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.dto.UpdateTenantDto;
import com.jd.workflow.console.dto.importer.ImportDto;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.impl.MethodVersionModifyLogServiceImpl;
import com.jd.workflow.console.service.doc.importer.JapiDataSyncService;
import com.jd.workflow.console.service.doc.importer.JapiHttpDataImporter;
import com.jd.workflow.console.service.doc.importer.JddjApiImporter;
import com.jd.workflow.console.service.doc.importer.UpstandardSyncService;
import com.jd.workflow.console.service.doc.importer.dto.JApiProjectInfo;
import com.jd.workflow.console.service.doc.importer.dto.JApiProjectResult;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.method.MethodModifyDeltaInfoService;
import com.jd.workflow.console.service.plugin.HotswapDeployInfoService;
import com.jd.workflow.console.service.sync.AppType;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 每次上线后需要执行一些更新的sql语句，放到这里面执行
 */
@Slf4j
@RestController
@UmpMonitor
@RequestMapping("/update")
@Api(hidden = true,value = "更新信息")
public class UpdateDataController {
    @Autowired
    MethodVersionModifyLogServiceImpl methodVersionModifyLogService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IInterfaceVersionService versionService;
    @Resource
    IMemberRelationService memberRelationService;
    @Autowired
    JddjApiImporter jddjApiImporter;
    @Autowired
    IUserInfoService userInfoService;
    @Autowired
    JapiDataSyncService japiDataSyncService;

    @Autowired(required = false)
    BusInterfaceRpcService rpcService;
    @Autowired
    JapiHttpDataImporter japiHttpDataImporter;
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    UpstandardSyncService upstandardSyncService;
    @Autowired
    HotswapDeployInfoService hotswapDeployInfoService;

    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    MethodModifyDeltaInfoService methodModifyDeltaInfoService;
    @PostMapping("/updateTenantId")
    @ApiOperation(value="更新租户信息",hidden = true)
    public CommonResult updateTenantId(){
        String tenantId = UserSessionLocal.getUser().getTenantId();
        log.info("update_tenant_id::tenantid={}",tenantId);
        {
            LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();
            luw.set(InterfaceManage::getTenantId, tenantId);
            interfaceManageService.update(luw);
        }
        {
            LambdaUpdateWrapper<AppInfo> appLqw = new LambdaUpdateWrapper<>();
            appLqw.set(AppInfo::getTenantId,tenantId);
            appInfoService.update(appLqw);
        }
        //4.出参
        return CommonResult.buildSuccessResult(1);
    }
    @PostMapping("/updateUserDeptInfo")
    @ApiOperation(value="更新租户信息",hidden = true)
    public CommonResult updateUserDeptInfo(){
        validateHasAuth();
        userInfoService.updateUserDeptInfo();
        //4.出参
        return CommonResult.buildSuccessResult(1);
    }
    @GetMapping("/updateJsfInterfaceCode")
    @ApiOperation(value="更新jsf serviceCode信息",hidden = true)
    public CommonResult updateJsfInterfaceCode(){
        LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();
        luw.eq(InterfaceManage::getType,InterfaceTypeEnum.JSF.getCode());
        luw.isNull(InterfaceManage::getServiceCode);
        luw.setSql("service_code = name");
        //luw.set(InterfaceManage::getServiceCode,InterfaceManage::getName);
        interfaceManageService.update(luw);
        //4.出参
        return CommonResult.buildSuccessResult(1);
    }

    @PostMapping("/updateInterfaceTenantId")
    @ApiOperation(value="更新租户信息",hidden = true)
    public CommonResult updateInterfaceTenantId(@RequestBody UpdateTenantDto dto){
        validateHasAuth();
        List<InterfaceManage> interfaceManages = interfaceManageService.listByIds(dto.getInterfaceIds());
        for (InterfaceManage interfaceManage : interfaceManages) {
            interfaceManage.setTenantId(dto.getTenantId());
            interfaceManageService.updateById(interfaceManage);
        }
        //4.出参
        return CommonResult.buildSuccessResult(1);
    }

    @RequestMapping("/updateAppTenantId")
    @ApiOperation(value="更新租户信息",hidden = true)
    public CommonResult updateAppTenantId(Long appId){
        validateHasAuth();
        AppInfo appInfo = appInfoService.getById(appId);
        Guard.notNull(appInfo,"无效的应用");
        appInfo.setTenantId(UserSessionLocal.getUser().getTenantId());
        appInfoService.updateById(appInfo);
        interfaceManageService.getAppInterface(appId).forEach(interfaceManage -> {
            interfaceManage.setTenantId(appInfo.getTenantId());
            interfaceManageService.updateById(interfaceManage);
        });
        //4.出参
        return CommonResult.buildSuccessResult(1);
    }
    @PostMapping("/initInterfaceVersion")
    @ApiOperation(value="初始化版本信息",hidden = true)
    public CommonResult initInterfaceVersion(){
        String tenantId = UserSessionLocal.getUser().getTenantId();
        log.info("update_tenant_id::tenantid={}",tenantId);
        LambdaUpdateWrapper<InterfaceManage> lqw = new LambdaUpdateWrapper<>();
        lqw.isNull(InterfaceManage::getLatestDocVersion);
         List<InterfaceManage> list = interfaceManageService.list(lqw);
        for (InterfaceManage manage : list) {
            if(InterfaceTypeEnum.HTTP.getCode().equals(manage.getType())
                    || InterfaceTypeEnum.JSF.getCode().equals(manage.getType())
                    ||InterfaceTypeEnum.EXTENSION_POINT.getCode().equals(manage.getType())
            ){
                versionService.initInterfaceVersion(manage);
                interfaceManageService.updateById(manage);
            }
        }

        //4.出参
        return CommonResult.buildSuccessResult(list.size());
    }
    @PostMapping("/initInterfaceApp")
    @ApiOperation(value="初始化接口应用",hidden = true)
    public CommonResult initInterfaceApp(){
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.isNotNull(InterfaceManage::getCjgAppId);
        List<InterfaceManage> list = interfaceManageService.list(lqw);
        for (InterfaceManage interfaceManage : list) {
            if(interfaceManage.getAppId() != null) continue;
             AppInfo app = appInfoService.findApp(interfaceManage.getCjgAppId());
             if(app != null){
                 app.setTenantId(interfaceManage.getTenantId());
                 appInfoService.updateById(app);
                 interfaceManage.setAppId(app.getId());
                 interfaceManageService.updateById(interfaceManage);
                 log.info("interface.update_app:appCode={},app={}",interfaceManage.getCjgAppId(),JsonUtils.toJSONString(app));
             }
        }
        //4.出参
        return CommonResult.buildSuccessResult(1);
    }
    @PostMapping("/initAppInfo")
    @ApiOperation(value="初始化应用信息",hidden = true)
    public CommonResult initAppInfo(@CookieValue(name="sso.jd.com") String cookie,int pageSize,@RequestBody List<String> cjgAppCodes){
        if(cjgAppCodes == null || cjgAppCodes.isEmpty()){
            cjgAppCodes = getCjgAppCode(cookie,pageSize);
        }

        for (String cjgAppCode : cjgAppCodes) {
            appInfoService.syncCjgAppToLocal(cjgAppCode,false);
        }

        //4.出参
        return CommonResult.buildSuccessResult(1);
    }
    List<String> fetchCjgAppCodesByQueryResult(String result){
        final Map map = JsonUtils.parse(result, Map.class);
        final Map data = (Map) map.get("data");
        List<String> ret = new ArrayList<>();
        List<Map<String,Object>> datas = (List<Map<String,Object>>) data.get("data");
        for (Map<String, Object> obj : datas) {
            String name = (String) obj.get("name");
            ret.add(name);
        }
        return ret;
    }
    private List<String> getCjgAppCode(String ssoCookie,int pageSize){
        //String cjgAppUrl = "http://test.cjg-api.jd.com";
        String cjgAppUrl = "http://cjg-api.jd.com";
        Map<String,Object> headers = new HashMap<>();

        headers.put("Cookie","sso.jd.com="+ssoCookie);
        // headers.put("Cookie","sso.jd.com=BJ.15672E478F37CE24FC7759F7732D79F9.6720221208092221");
        RequestClient client = new RequestClient(cjgAppUrl,headers);
        final String result = client.get("/api/component/searchCurrentApp?start=0&pageSize="+pageSize, null); //&source=
        log.info("app.fetch_cjg_app_result={}",result);
        return fetchCjgAppCodesByQueryResult(result);
    }

    @ApiOperation(value="导入jsf",hidden = true)
    @PostMapping("/importDjApi")
    public CommonResult importDjApi(@CookieValue(name="sso.jd.com") String cookie,String appType,
                                    @RequestBody ImportDto dto){
        jddjApiImporter.importDjApp(cookie,dto, AppType.valueOf(appType));
        return CommonResult.buildSuccessResult(true);
    }

    @ApiOperation(value="导入信鸽api应用",hidden = true)
    @PostMapping("/syncDjApps")
    public CommonResult syncDjApps(@CookieValue(name="sso.jd.com") String cookie
                                    ){
        validateHasAuth();
        jddjApiImporter.syncDjApp(cookie);
        return CommonResult.buildSuccessResult(true);
    }
    @ApiOperation(value="导入应用数据",hidden = true)
    @PostMapping("/importAppData")
    public CommonResult importAppData(@CookieValue(name="sso.jd.com") String cookie,String appType,
                                      @RequestBody List<String> appCodes
    ){
        validateHasAuth();
        jddjApiImporter.importJddjApp(cookie,AppType.valueOf(appType),appCodes);
        return CommonResult.buildSuccessResult(true);
    }
    @ApiOperation(value="清理数据",hidden = true)
    @GetMapping("/clearApi")
    public CommonResult clearImportedApi(
                                    Long interfaceId){
        validateHasAuth();
        jddjApiImporter.clearImportData(Collections.singletonList(interfaceId));
        return CommonResult.buildSuccessResult(true);
    }

    @ApiOperation(value="清理数据",hidden = true)
    @GetMapping("/clearHttpAuthData")
    public CommonResult clearHttpAuthData(
            String appCode){
        validateHasAuth();
        jddjApiImporter.clearAppAuthData(appCode);
        return CommonResult.buildSuccessResult(true);
    }
    private void validateHasAuth(){
        String userId = UserSessionLocal.getUser().getUserId();
        if(!"wangjingfang3".equals(userId) && !"xinwengang".equals(userId)) {
            throw new BizException("无权限");
        }
    }
    @ApiOperation(value="清理京东到家接口数据",hidden = true)
    @GetMapping("/clearDjApiData")
    public CommonResult clearDjApiData(
            ){
        validateHasAuth();
        jddjApiImporter.cleanDjImportedData();
        return CommonResult.buildSuccessResult(true);
    }
    @ApiOperation(value="导入工程数据测试",hidden = true)
    @GetMapping("/syncJapiAppTest")
    public CommonResult syncJapiAppTest(
            @RequestParam(required = false) Boolean forceUpdateApp

    ){
        if(forceUpdateApp == null)
            forceUpdateApp = false;
        MetaContextHelper.skipModify(true);
       japiHttpDataImporter.syncJapiApp(null,1,1,forceUpdateApp);;
       MetaContextHelper.clearModifyState();
        return CommonResult.buildSuccessResult(true);
    }
    @ApiOperation(value="导入工程数据",hidden = true)
    @GetMapping("/importProjectData")
    public CommonResult importProjectData(

        @RequestParam(required = false) Boolean forceUpdateApp
    ){
        if(forceUpdateApp == null)
            forceUpdateApp = false;
       japiHttpDataImporter.initJApiApp(forceUpdateApp);
        // japiHttpDataImporter.syncJapiApp(null,1,1);;
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 初始化j-api接口以及需求信息
     * @hidden
     * @param japiId
     * @return
     */
    @RequestMapping("initJapiInterfaces")
    public CommonResult<Boolean> initJapiInterfaces(@RequestParam(required = false) Long japiId){
        japiHttpDataImporter.initJapiInterfaces(japiId);
        return CommonResult.buildSuccessResult(true);
    }


   /* @ApiOperation(value="初始化接口信息",hidden = true)
    @PostMapping("/initProjectInterfaceInfo")
    public CommonResult initProjectInterfaceInfo(
        @RequestBody List<String> appCodes,Boolean forceUpdate
    ){
        if(forceUpdate == null){
            forceUpdate = false;
        }
        japiHttpDataImporter.initProjectInterfaceInfo(appCodes,forceUpdate);
        return CommonResult.buildSuccessResult(true);
    }*/
    @ApiOperation(value="导入数据",hidden = true)
    @GetMapping("/importJapiData")
    public CommonResult importJapiData(
            @RequestBody List<Long> appIds
    ){

         JApiProjectInfo project = japiHttpDataImporter.getProject(appIds.get(0));
        return CommonResult.buildSuccessResult(true);
    }
    @ApiOperation(value="清理japi导入的数据",hidden = true)
    @GetMapping("/clearJapiData")
    public CommonResult clearJapiData(

    ){

       japiHttpDataImporter.cleanDjImportedData();
        return CommonResult.buildSuccessResult(true);
    }


    @ApiOperation(value="更新接口部门信息",hidden = true)
    @GetMapping("/updateInterfaceDeptName")
    public CommonResult updateInterfaceDeptName(
    ){
        validateHasAuth();
        jddjApiImporter.updateInterfaceDeptName();
        return CommonResult.buildSuccessResult(true);
    }
    @PostMapping("/unBinding")
    @ApiOperation(value = "成员管理 删除成员",hidden=true)
    public CommonResult<Boolean> unBinding(@RequestBody MemberRelationDTO memberRelationDTO) {
        log.info("MemberRelationController unBinding query={}", JsonUtils.toJSONString(memberRelationDTO));
        validateHasAuth();
        //3.service层
        Boolean result = memberRelationService.unBinding(memberRelationDTO);
        //4.出参
        return CommonResult.buildSuccessResult(result);
    }

    /**
     * @hidden
     * @return
     */
    @GetMapping("/updateMethodDigest")
    public CommonResult<Boolean> updateMethodDigest(){
        validateHasAuth();
        methodManageService.updateMethodDigest();
       return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/syncJapiInfo")
    public CommonResult<Boolean> syncJapiInfo(Long interfaceId,boolean forceUpdate){
        japiDataSyncService.syncJapiInterface(interfaceId,forceUpdate);
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/clearInterfaceRelatedId")
    public CommonResult<Boolean> clearInterfaceRelatedId(){
        japiDataSyncService.clearInterfaceRelatedId();
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/initProjectInfoById")
    public CommonResult<Long> initProjectInfoById(Long projectId){
        Long interfaceId = japiHttpDataImporter.initJapiProjectInterface(projectId);
        return CommonResult.buildSuccessResult(interfaceId);
    }
    @GetMapping("/clearInvalidInterface")
    public CommonResult<Boolean> clearInvalidInterface(){
        japiDataSyncService.clearInvalidInterface();
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/clearInvalidRequirement")
    public CommonResult<Boolean> clearInvalidRequirement(){
        japiDataSyncService.clearInvalidRequirement();
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/markClearInterfaceIds")
    public CommonResult<Boolean> markClearInterfaceIds(@RequestBody List<Long> interfaceIds){
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getYn,1);
        lqw.in(InterfaceManage::getId,interfaceIds);
        List<InterfaceManage> interfaceManages = interfaceManageService.list(lqw);
        for (InterfaceManage interfaceManage : interfaceManages) {
            interfaceManage.setYn(0);
            interfaceManageService.updateById(interfaceManage);
        }
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/updateRequirementInfoRelatedId")
    public CommonResult<Boolean> updateRequirementInfoRelatedId(){
        japiDataSyncService.updateRequirementInfoRelatedId();
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/updateInterfaceManageRelatedId")
    public CommonResult<Boolean> updateInterfaceManageRelatedId(){
        japiDataSyncService.updateInterfaceManageRelatedId();
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/updateUserTenantId")
    public CommonResult<Boolean> updateUserTenantId(Long sceneId,String tenantId){
        upstandardSyncService.updateUserTenantId(sceneId, tenantId);
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/syncAppInfo")
    public CommonResult<Boolean> syncAppInfo(@CookieValue(name="sso.jd.com") String cookie){
        japiDataSyncService.syncAppInfo(cookie);
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/addAdmin")
    public CommonResult<Boolean> addAdmin(String erp){
        validateHasAuth();
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(ResourceTypeEnum.TENANT_ADMIN.getCode());
        memberRelationDTO.setResourceId(0L);
        memberRelationDTO.setUserCode(erp);
        memberRelationDTO.setResourceRole(ResourceRoleEnum.TENANT_ADMIN.getCode());
        memberRelationService.binding(memberRelationDTO);
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/updateReportDelta")
    public CommonResult<Boolean> updateReportDelta(String erp){
        validateHasAuth();
        methodManageService.updateReportDelta();
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/updateMethodDeltaInfo")
    public CommonResult<Boolean> updateMethodDeltaInfo(String erp){
        validateHasAuth();
        methodModifyDeltaInfoService.updateInterfaceIdAndNameInfo();
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/updateInterfaceJapiMockInfo")
    public CommonResult<Boolean> updateInterfaceJapiMockInfo(Long interfaceId){
        validateHasAuth();
        japiHttpDataImporter.updateInterfaceJapiMockInfo(interfaceId);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * @hidden
     * @return
     */
    @GetMapping("/updateAllJsfInterfaceNameToServiceCode")
    public CommonResult<Boolean> updateAllJsfInterfaceNameToServiceCode(){
        validateHasAuth();
        methodManageService.updateAllJsfInterface();
        return CommonResult.buildSuccessResult(true);
    }
    /**
     * @hidden
     * @return
     */
    @GetMapping("/updateAllJsfMethodNameToMethodCode")
    public CommonResult<Boolean> updateAllJsfMethodNameToMethodCode(){
        validateHasAuth();
        methodManageService.updateAllJsfMethod();
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/initHotDeployUser")
    public CommonResult<Boolean> initHotDeployUser(){
       validateHasAuth();
       hotswapDeployInfoService.initUser();
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/syncProjectMembers")
    public CommonResult<Boolean> syncProjectMembersToApp(Long interfaceId){
        validateHasAuth();
        japiHttpDataImporter.initInterfaceProjectMembers(interfaceId);
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/initProjectMembers")
    public CommonResult<Boolean> initProjectMembers(){
        validateHasAuth();
        japiHttpDataImporter.initJapiAppInterfaces();
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/updateInterfaceAppId")
    public CommonResult<Boolean> updateInterfaceAppId(Long interfaceId,Long appId){
        validateHasAuth();
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        Guard.notEmpty(interfaceManage,"无效的接口");
        interfaceManage.setAppId(appId);
        interfaceManageService.updateById(interfaceManage);
        return CommonResult.buildSuccessResult(true);
    }
    @GetMapping("/removeMethodModifyLog")
    public CommonResult<Boolean> removeMethodModifyLog(Long id){
        validateHasAuth();
       methodVersionModifyLogService.removeModifyLog(id);
        return CommonResult.buildSuccessResult(true);
    }
    /*@RequestMapping("markDeleteInterface")
    public CommonResult<Boolean> markDeleteInterface(@RequestBody  List<Long> interfaceIds){

        return CommonResult.buildSuccessResult(true);
    }*/
    public static void main(String[] args) {
        String cjgAppUrl = "http://cjg-api.jd.com";
        Map<String,Object> headers = new HashMap<>();

        //headers.put("Cookie",ssoCookie);
        headers.put("Cookie","sso.jd.com=BJ.15672E478F37CE24FC7759F7732D79F9.6720221208092221");
        RequestClient client = new RequestClient(cjgAppUrl,headers);
        final String result = client.get("/api/component/searchCurrentApp?start=0&pageSize=10&name=&source=2", null);
        log.info("app.fetch_cjg_app_result={}",result);
    }
}
