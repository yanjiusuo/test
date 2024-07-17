package com.jd.workflow.console.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.annotation.UmpMonitor;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.app.AppMembers;
import com.jd.workflow.console.dto.app.CjgAppDto;
import com.jd.workflow.console.dto.manage.AppSearchResult;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.AppInfoMembers;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.service.IAppInfoMembersService;
import com.jd.workflow.console.service.impl.AppInfoServiceImpl;
import com.jd.workflow.server.dto.QueryResult;
import com.jd.workflow.server.dto.interfaceManage.*;
import com.jd.workflow.server.service.InterfaceManageRpcService;
import com.jd.workflow.soap.common.lang.Guard;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 应用管理
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/appInfo")
@UmpMonitor
@Api(value = "应用管理", tags = "应用管理")
public class AppInfoController {

    @Resource
    private AppInfoServiceImpl appInfoService;
    @Autowired
    private CjgHelper cjgHelper;
    @Resource
    private InterfaceManageRpcService interfaceManageRpcService;

    @Autowired
    private IAppInfoMembersService appInfoMembersService;
    /**
     * @title 添加藏经阁应用
     * @description 添加藏经阁应用管理
     * @param dto
     * @return
     */
   /* @PostMapping("/addCjgApp")
    //@ApiOperation("添加应用")
    public CommonResult<Long> addCjgApp(@RequestBody CjgAppDto dto){
        log.info("AppInfoController addApp requestBody={} ", JSON.toJSONString(dto));
        Guard.notNull(dto,"新增应用时入参不能为空");
        return addApp(dto.toAppInfoDto());
    }
    @PostMapping("/modifyCjgApp")
    @ApiOperation("添加应用")
    public CommonResult<Boolean> modifyCjgApp(@RequestBody CjgAppDto dto){

        log.info("AppInfoController addApp requestBody={} ", JSON.toJSONString(dto));
        Guard.notNull(dto,"新增应用时入参不能为空");
        return modifyApp(dto.toAppInfoDto());
    }*/

    /**
     * @param id 应用id
     * @return
     */
    @RequestMapping("/getCjgAppById")
    @ApiOperation("获取应用详情")
    public CommonResult<CjgAppDto> getCjgAppById(Long id) {
        log.info("AppInfoController addApp requestBody={} ", JSON.toJSONString(id));
        Guard.notNull(id, "id不能为空");
        final AppInfoDTO dto = appInfoService.findApp(id);
        Guard.notEmpty(dto, "应用不存在");
        return CommonResult.buildSuccessResult(CjgAppDto.newCjgAppDto(dto));
    }

    /**
     * 添加应用
     *
     * @param dto
     * @return
     */
    @PostMapping("/addApp")
    @ApiOperation("添加应用")
    public CommonResult<Long> addApp(@RequestBody JdosAppInfoDto dto) {
        log.info("AppInfoController addApp requestBody={} ", JSON.toJSONString(dto));
        Guard.notNull(dto, "新增应用时入参不能为空");
        return CommonResult.buildSuccessResult(appInfoService.addApp(dto.toAppInfoDto(true)));
    }

    @GetMapping("/createCjgApp")
    @ApiOperation("添加应用")
    public CommonResult<Long> createCjgApp(Long appId) {

        AppInfoDTO dto = appInfoService.findApp(appId);
        dto.setAuthLevel("0");
        String cjgId = cjgHelper.createCjgComponent(dto);
        AppInfo app = appInfoService.getById(appId);
        app.setCjgAppId(cjgId);
        appInfoService.updateById(app);
        return CommonResult.buildSuccessResult(appId);
    }

    @PostMapping("/modifyApp")
    @ApiOperation(value = "修改应用")
    public CommonResult<Boolean> modifyApp(@RequestBody JdosAppInfoDto dto) {
        log.info("AppInfoController modifyApp requestBody={} ", JSON.toJSONString(dto));
        Guard.notNull(dto, "修改应用时入参不能为空");
        return CommonResult.buildSuccessResult(appInfoService.modifyApp(dto.toAppInfoDto(false)));
    }


    /**
     * @param jdosAppCode jdos应用code
     * @return jdos应用成员
     */
    @GetMapping("/getJdosAppMembers")
    @ApiOperation(value = "获取jdos应成员")
    public CommonResult<AppMembers> getJdosAppMembers(String jdosAppCode) {
        log.info("AppInfoController getJdos_App_members appCode={} ", jdosAppCode);
        Guard.notNull(jdosAppCode, "应用编码不可为空");
        return CommonResult.buildSuccessResult(appInfoService.getJdosAppMembers(jdosAppCode));
    }

    @GetMapping("/removeApp")
    @ApiOperation(value = "移除应用")
    public CommonResult<Boolean> removeApp(Long id, @CookieValue("sso.jd.com") String cookieValue) {
        log.info("AppInfoController removeApp id={} ", id);
        Guard.notNull(id, "删除App时id不能为空");
        return CommonResult.buildSuccessResult(appInfoService.removeApp(id, cookieValue));
    }

    @GetMapping("/realRemoveApp")
    @ApiOperation(value = "移除应用", hidden = true)
    public CommonResult<Boolean> realRemoveApp(Long id) {
        log.info("AppInfoController removeApp id={} ", id);
        Guard.notNull(id, "删除App时id不能为空");
        return CommonResult.buildSuccessResult(appInfoService.realRemoveApp(id));
    }

    @GetMapping("/recoveryApp")
    @ApiOperation(value = "恢复应用", hidden = true)
    public CommonResult<Boolean> recoveryApp(Long id) {
        log.info("AppInfoController recoveryApp id={} ", id);
        Guard.notNull(id, "删除App时id不能为空");
        AppInfo app = appInfoService.getById(id);
        app.setYn(1);
        appInfoService.updateById(app);
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/findApp")
    @ApiOperation(value = "根据id获取应用信息")
    public CommonResult<JdosAppInfoDto> findApp(Long id) {
        log.info("AppInfoController findApp id={} ", id);
        Guard.notNull(id, "查询App时id不能为空");
        AppInfoDTO appInfoDto = appInfoService.findApp(id);
        return CommonResult.buildSuccessResult(JdosAppInfoDto.from(appInfoDto));
    }

    /**
     * @param appCode 应用编码
     * @return
     */
    @GetMapping("/findAppByCode")
    @ApiOperation(value = "根据appCode获取应用信息")
    public CommonResult<AppInfoDTO> findAppByCode(String appCode) {
        log.info("AppInfoController findApp code={} ", appCode);
        Guard.notNull(appCode, "查询App时code不能为空");
        return CommonResult.buildSuccessResult(appInfoService.findAppByCode(appCode));
    }

    @PostMapping("/updateAppTenantId")
    @ApiOperation("更新应用租户id")
    public CommonResult<Boolean> updateAppTenant(@RequestBody UpdateAppTenant tenant) {
        log.info("AppInfoController findApp tenant={} ", tenant);
        Guard.notNull(tenant, "body不可为空");
        appInfoService.updateAppTenant(tenant);
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/syncAppMember")
    @ApiOperation(value = "更新应用成员", hidden = true)
    public CommonResult<Boolean> syncAppMember(Long appId) {
        log.info("AppInfoController syncAppMember tenant={} ", appId);
        Guard.notNull(appId, "body不可为空");
        appInfoService.syncJdosMembers(appId);
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/setJdosMember")
    @ApiOperation(value = "更新应用成员", hidden = true)
    public CommonResult<Boolean> setJdosMember(Long appId, String jdosAppCode) {
        AppInfo app = appInfoService.getById(appId);
        app.setJdosAppCode(jdosAppCode);
        appInfoService.updateById(app);
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/initJdosApp")
    @ApiOperation(value = "初始化jdos成员", hidden = true)
    public CommonResult<Boolean> initJdosApp() {
        log.info("AppInfoController initJdosApp tenant ");
        appInfoService.initJdosApp();
        return CommonResult.buildSuccessResult(true);
    }

    @GetMapping("/initJdosAppAndMembers")
    @ApiOperation(value = "初始化jdos成员", hidden = true)
    public CommonResult<Boolean> initJdosAppAndMembers() {
        log.info("AppInfoController initJdosApp tenant ");
        appInfoService.initJdosAppAndMembers();
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 校验密钥是否有效
     *
     * @param appCode
     * @param appSecret
     * @return
     */
    @GetMapping("/checkSecret")
    public CommonResult<Boolean> checkSecret(String appCode, String appSecret) {
        log.info("AppInfoController checkSecret appCode={} , appSecret={}", appCode, appSecret);
        Guard.notEmpty(appCode, "校验应用秘钥时appCode不能为空");
        Guard.notEmpty(appSecret, "校验应用秘钥时appSecret不能为空");
        return CommonResult.buildSuccessResult(appInfoService.checkSecret(appCode, appSecret));
    }

    /**
     * 查询应用信息
     *
     * @param query
     * @return
     */
    @PostMapping("/queryApp")
    public CommonResult<QueryAppResultDTO> queryApp(@RequestBody QueryAppReqDTO query, HttpServletRequest request) {
        log.info("AppInfoController queryApp requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询应用列表时入参不能为空");
        //TODO pin管理员
        query.setTenantId(UserSessionLocal.getUser().getTenantId());
        query.setPin(UserSessionLocal.getUser().getUserId());
        String tenantId = request.getHeader("tenantId");
        QueryAppResultDTO queryAppResultDTO = null;
        if (StringUtils.isNotBlank(tenantId)) { // 集成到服务总线的页面是通过请求头加tenantId来区分的
            queryAppResultDTO = appInfoService.queryAppByCondition(query);
        } else {
            queryAppResultDTO = appInfoService.queryHasNoApp(query);
        }


        return CommonResult.buildSuccessResult(queryAppResultDTO);
    }

    @PostMapping("/queryAppDebug")
    public CommonResult<QueryAppResultDTO> queryAppDebug(@RequestBody QueryAppReqDTO query, HttpServletRequest request) {
        log.info("AppInfoController queryApp requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询应用列表时入参不能为空");
        //TODO pin管理员
        query.setTenantId(UserSessionLocal.getUser().getTenantId());
//        query.setPin(UserSessionLocal.getUser().getUserId());
        String tenantId = request.getHeader("tenantId");
        QueryAppResultDTO queryAppResultDTO = null;
        if (StringUtils.isNotBlank(tenantId)) { // 集成到服务总线的页面是通过请求头加tenantId来区分的
            queryAppResultDTO = appInfoService.queryAppByCondition(query);
        } else {
            queryAppResultDTO = appInfoService.queryHasNoApp(query);
        }


        return CommonResult.buildSuccessResult(queryAppResultDTO);
    }

    /**
     * 查询应用信息
     *
     * @param search       要搜索的字符串
     * @param onlySelf     0=全部应用 1个人应用
     * @param includeNoApp 是否包含无应用
     * @return
     */
    @GetMapping("/searchApp")
    public CommonResult<List<AppSearchResult>> searchApp(String search, Long id, @RequestParam(required = false) Integer onlySelf, @RequestParam(required = false) Integer includeNoApp) {
        if (onlySelf == null) onlySelf = 1;
        return CommonResult.buildSuccessResult(appInfoService.searchApp(search, id, onlySelf, includeNoApp));
    }

    /**
     * 查询用户java bean应用列表
     *
     * @param name 应用code或者应用名称
     * @return
     */
    @GetMapping("queryBeanApp")
    public CommonResult<List<AppInfo>> queryApps(String name) {
        List<AppInfo> appInfos = appInfoService.queryBeanApps(name);
        return CommonResult.buildSuccessResult(appInfos);
    }

    /**
     * 查询藏经阁应用
     *
     * @param query
     * @return
     */
    @PostMapping("/queryCjgApp")
    public CommonResult<QueryAppResultDTO> queryCjgApp(@RequestBody QueryAppReqDTO query) {
        log.info("AppInfoController queryCjgApp requestBody={} ", JSON.toJSONString(query));
        Guard.notNull(query, "查询应用列表时入参不能为空");
        query.setTenantId(UserSessionLocal.getUser().getTenantId());
        query.setPin(UserSessionLocal.getUser().getUserId());
        return CommonResult.buildSuccessResult(appInfoService.queryImportSysApp(query));
    }
    //http://wfg.jd.com:8010//appInfo/sysAppInfoMem

    /**
     * @param appCode
     * @return
     * @hidden 同步应用成员
     */
    @GetMapping("/sysAppInfoMem")
    public CommonResult<Boolean> queryCjgApp(@RequestParam(required = false) String appCode) {
        appInfoService.sysAppInfoMem(appCode);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * @param appId
     * @return
     * @hidden 同步应用成员
     */
    @RequestMapping("/updateAppTrace")
    public CommonResult<Boolean> queryCjgApp(Long appId, @RequestBody List<String> cookie) {
        AppInfo appInfo = appInfoService.getById(appId);
        appInfoService.updateAppTrace(appInfo, cookie.get(0));
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * @param
     * @return
     * @hidden 同步应用成员
     */
    @RequestMapping("/updateAllAppTrace")
    public CommonResult<Boolean> updateAllAppTrace(@RequestBody List<String> cookie) {
        appInfoService.updateAllAppTrace(cookie.get(0));
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * @return
     * @hidden 同步jdos成员
     */
    @GetMapping("/syncJdosAppMembers")
    public CommonResult<Boolean> syncJdosAppMembers() {
        appInfoService.syncJdosMembers();
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * 前端未调用，手动更新数据
     *
     * @param info
     * @return
     */
    @GetMapping("/syncDeptInfo")
    public CommonResult<Boolean> syncDeptInfoTest(AppInfo info) {
        appInfoService.syncDeptt(info);
        return CommonResult.buildSuccessResult(true);
    }

    @PostMapping("/modifyInfoYn")
    public CommonResult<List<Long>> modifyInfoYn(@RequestBody AppInfo info) {
        List<Long> st = appInfoService.modifyInfoYn(info, null, info.getYn());
//        if(StringUtils.isNotBlank(ids)){
//            appInfoService.removeById(Arrays.asList(ids.split(",")).stream().map(i->Long.valueOf(i)).collect(Collectors.toList()));
//        }
        return CommonResult.buildSuccessResult(st);
    }

    @PostMapping("/modifyInfoBat")
    public CommonResult<List<Long>> modifyInfoYn(@RequestBody List<Map<String, String>> ids) {
        List<Long> st = appInfoService.modifyInfoYn(null, ids, 0);
//        if(StringUtils.isNotBlank(ids)){
//            appInfoService.removeById(Arrays.asList(ids.split(",")).stream().map(i->Long.valueOf(i)).collect(Collectors.toList()));
//        }
        return CommonResult.buildSuccessResult(st);
    }


    /**
     * @param appId
     * @return
     * @hidden
     */
    @GetMapping("/changeOwnerToJdosOwner")
    public CommonResult<Boolean> changeOwnerToJdosOwner(Long appId) {
        AppInfo appInfo = appInfoService.getById(appId);
        appInfoService.changeOwnerToJdosOwner(appInfo);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * @param appId
     * @return
     * @hidden
     */
    @GetMapping("/syncOwnerAndProductorFromCjg")
    public CommonResult<Boolean> syncOwnerAndProductorFromCjg(Long appId) {
        AppInfo appInfo = appInfoService.getById(appId);
        appInfoService.syncOwnerAndProductorFromCjg(appInfo);
        return CommonResult.buildSuccessResult(true);
    }

    /**
     * @return
     * @hidden
     */
    @GetMapping("/resetAllMember")
    public CommonResult<Boolean> resetAllMember() {
        appInfoService.resetAllMember();
        return CommonResult.buildSuccessResult(true);
    }


    @GetMapping("/testJsf")
    public CommonResult<List<JsfBatchInterface>> resetAllMember(String text, Long appId, String ip, String serviceCode) {
        List<JsfBatchInterface> groups = new ArrayList();
        JsfBatchInterface interface1 = new JsfBatchInterface();
        JsfInterfaceManage firstDict = new JsfInterfaceManage();
        firstDict.setName(ip);
        firstDict.setAdminCode("zhangqian346");
        firstDict.setAppId(appId);
        firstDict.setLevel(1);
        firstDict.setServiceCode(ip);
        firstDict.setTenantId("up_jd");
        firstDict.setType(22);
        JsfDocConfigDto docConfig = new JsfDocConfigDto();
        docConfig.setDocType("md");
        firstDict.setDocConfig(docConfig);
        List<JsfEnvModel> envInfo = new ArrayList<>();
        JsfEnvModel model = new JsfEnvModel();
        model.setEnvName("测试环境");
        model.setType(EnvTypeEnum.TEST);
        List<String> urls = new ArrayList<>();
        urls.add("http://" + ip);
        model.setUrl(urls);
        envInfo.add(model);
        firstDict.setEnvInfo(envInfo);
        interface1.setFirstDic(firstDict);

        //四级方法


        JsfMethodManage method = new JsfMethodManage();
        method.setAppId(appId);
        method.setName("方法名称");
        method.setHttpMethod("POST");
        method.setMethodCode("tetMethod");
        method.setPath("/test/test111");
        method.setPublished(1);
        method.setType(22);
        JsfHttpMethodModel content = new JsfHttpMethodModel();
        content.setType("http");
        JsfHttpMethodInput input = new JsfHttpMethodInput();
        List<JsfJsonType> param1 = new ArrayList<>();
        JsfJsonType paramJson = JsfJsonType.newEntity("String");
        paramJson.setName("param1");
        paramJson.setRequired(true);
        paramJson.setDesc("测试字段1");

        JsfJsonType paramObject = JsfJsonType.newEntity("object");
        paramObject.setName("param3");
        paramObject.setRequired(true);
        paramObject.setDesc("测试字段2-object");
        JsfJsonType child = JsfJsonType.newEntity("String");
        child.setName("param1");
        child.setRequired(true);
        child.setDesc("测试字段1");

        List<JsfJsonType> children = new ArrayList<>();
        children.add(child);
        paramObject.setChildren(children);
        param1.add(paramJson);
        input.setParams(param1);
        input.setReqType("json");
        input.setMethod("post");
        List<JsfJsonType> body = new ArrayList<>();

        JsfJsonType paramJson2 = JsfJsonType.newEntity("String");
        paramJson2.setName("result");
        paramJson2.setRequired(true);
        paramJson2.setDesc("测试结果字段1");

        body.add(paramObject);
        body.add(paramJson2);
        input.setBody(body);
        content.setInput(input);

        List<JsfJsonType> bodyOUt = new ArrayList<>();
        JsfJsonType outType = JsfJsonType.newEntity("String");
        outType.setName("result");
        outType.setRequired(true);
        bodyOUt.add(outType);
        JsfHttpMethodOutput outPut = new JsfHttpMethodOutput();
        outPut.setBody(bodyOUt);
        content.setOutput(outPut);
        method.setContent(content);
        JsfMethodDocConfig docConfig1 = new JsfMethodDocConfig();
        docConfig1.setDocType("md");
        method.setDocConfig(docConfig1);
        //三级目录
        List<JsfInterfaceMethodGroup> childGroups = new ArrayList<>();
        JsfInterfaceMethodGroup group3 = new JsfInterfaceMethodGroup();
        group3.setName(serviceCode);
        group3.setEnName(serviceCode);

        group3.setType(1);
        //四级方法
        List<JsfMethodManage> methods4 = new ArrayList<>();
        group3.setThirdMethods(methods4);
        childGroups.add(group3);
        //二级目录
        JsfInterfaceMethodGroup group = new JsfInterfaceMethodGroup();
        group.setName(serviceCode);
        group.setEnName(serviceCode);
        group.setType(1);
        group.setChildDic(childGroups);
        //三级方法
        List<JsfMethodManage> methods = new ArrayList<>();
        group.setThirdMethods(methods);
        //二级分组
        List<JsfInterfaceMethodGroup> secondeDict = new ArrayList<>();
        secondeDict.add(group);
        interface1.setSecondDic(secondeDict);
        //二级方法
        methods.add(method);
        interface1.setSecondMethods(methods);
        groups.add(interface1);
        try {
            log.info("测试入口-->{}", groups);
        } catch (Exception e) {
            e.printStackTrace();
        }
        QueryResult<List<JsfBatchInterface>> restt = interfaceManageRpcService.addMethodBatch(appId, groups);
        return CommonResult.buildSuccessResult(restt.getData());
    }


    /**
     * 获取jdos code对应的应用信息
     *
     * @param jdosAppCode
     * @param erp
     * @return
     */
    @GetMapping("/InitByJdosCode")
    public List<AppInfo> InitByJdosCode(@RequestParam String jdosAppCode, @RequestParam String erp) {
        List<AppInfo> appInfos = appInfoService.InitByJdosCode(jdosAppCode, erp);
        return appInfos;
    }

    @GetMapping("/getUserApp")
    public List<AppInfoDTO> getUserApp(@RequestParam String erp, @RequestParam(required = false) String name) {
        QueryAppReqDTO query = new QueryAppReqDTO();
        //query.setTenantId("up_jd");
        query.setPin(erp);
        query.setCurrentPage(1);
        query.setName(name);

        query.setPageSize(40);
        QueryAppResultDTO queryAppResultDTO = null;
        queryAppResultDTO = appInfoService.queryAppByCondition(query);
        return queryAppResultDTO.getList();
    }

    /**
     * @param jdosAppCode jdos应用编码
     * @return
     */
    @GetMapping("/findByJdosAppCode")
    @ApiOperation(value = "根据jdosAppCode获取应用信息")
    public CommonResult<AppInfo> findByJdosAppCode(String jdosAppCode) {
        log.info("AppInfoController findByJdosAppCode code={} ", jdosAppCode);
        Guard.notNull(jdosAppCode, "查询App时code不能为空");
        return CommonResult.buildSuccessResult(appInfoService.findByJdosAppCode(jdosAppCode));
    }


    @GetMapping("/delAppMember")
    public CommonResult<Boolean> delAppMember(Long appId, String erp) {

        LambdaQueryWrapper<AppInfoMembers> qw = new LambdaQueryWrapper<>();
        qw.eq(AppInfoMembers::getAppId, appId).eq(AppInfoMembers::getErp, erp);
        List<AppInfoMembers> appInfoMembersList = appInfoMembersService.list(qw);
        if (CollectionUtils.isNotEmpty(appInfoMembersList)) {
            for (AppInfoMembers appInfoMembers : appInfoMembersList) {
                appInfoMembersService.removeById(appInfoMembers.getId());
            }
        }
        return CommonResult.buildSuccessResult(true);
    }

}
