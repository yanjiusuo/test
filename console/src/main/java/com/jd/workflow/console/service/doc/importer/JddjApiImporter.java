package com.jd.workflow.console.service.doc.importer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.InterfaceManageDTO;
import com.jd.workflow.console.dto.auth.InterfaceAuthFilter;
import com.jd.workflow.console.dto.doc.DocReportDto;
import com.jd.workflow.console.dto.importer.ImportDto;
import com.jd.workflow.console.dto.importer.JddjApp;
import com.jd.workflow.console.dto.importer.JddjResult;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.entity.doc.MethodModifyLog;
import com.jd.workflow.console.entity.doc.MethodVersionModifyLog;
import com.jd.workflow.console.entity.sync.DataSyncRecord;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.doc.*;
import com.jd.workflow.console.service.doc.impl.DocReportServiceImpl;
import com.jd.workflow.console.service.doc.importer.dto.DjApiGroup;
import com.jd.workflow.console.service.doc.importer.dto.DjJoneAppInfo;
import com.jd.workflow.console.service.remote.api.JagileService;
import com.jd.workflow.console.service.remote.api.dto.jagile.JagileMember;
import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;
import com.jd.workflow.console.service.sync.AppType;
import com.jd.workflow.console.service.sync.DataSyncRecordService;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.method.ClassMetadata;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 京东到家api导入
 */
@Slf4j
@Service
public class JddjApiImporter {
    static final String JDDJ_BASE_URL = "http://api.jddj.com";
    static final String API_SEARCH_URL = "/api/doc/search";
    static final String API_GET_URL = "/api/doc/get";
    static final String APP_GET_URL = "/config/app/search";
    static final String GROUP_GET_URL = "/config/app/group";


    static final String DJ_APP_PREFIX = "dj_";
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IMethodModifyLogService methodModifyLogService;
    @Autowired
    JagileService jagileService;

    @Autowired
    IMethodVersionModifyLogService methodVersionModifyLogService;
    @Autowired
    IMethodManageService methodManageService;
    @Autowired
    DocReportServiceImpl docReportService;

    @Autowired
    CjgHelper cjgHelper;
    @Autowired
    IAppInfoService appInfoService;

    @Autowired
    IInterfaceVersionService versionService;
    @Autowired
    SwaggerParserService swaggerParserService;

    @Resource(name = "docThreadExecutor")
    ScheduledThreadPoolExecutor scheduleService;

    @Autowired
    IHttpAuthService httpAuthService;

    @Autowired
    IHttpAuthDetailService httpAuthDetailService;
    @Autowired
    IHttpAuthApplyService httpAuthApplyService;
    @Autowired
    IHttpAuthConfigService httpAuthConfigService;
    @Autowired
    IHttpAuthApplyDetailService httpAuthApplyDetailService;
    @Autowired
    DataSyncRecordService dataSyncService;

    public void cleanDjImportedData() {
        List<Long> interfaceIds = getDjInterfaces();
        dataSyncService.clearSyncRecord("jddj");
        clearImportData(interfaceIds);
    }

    public List<Long> getDjInterfaces() {
        List<AppInfo> djApps = appInfoService.queryDjAppByPrefix(DJ_APP_PREFIX);
        List<Long> appIds = djApps.stream().map(vs -> vs.getId()).collect(Collectors.toList());
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.in(InterfaceManage::getAppId, appIds);
        List<InterfaceManage> interfaces = interfaceManageService.list(lqw);
        return interfaces.stream().map(vs -> vs.getId()).collect(Collectors.toList());
    }

    public void clearImportData(List<Long> interfaceIds) {
        //List<MethodManage> interfaceMethods = methodManageService.getInterfaceMethods(interfaceIds);
        LambdaQueryWrapper<MethodManage> methodLqw = new LambdaQueryWrapper<>();
        methodLqw.in(MethodManage::getInterfaceId, interfaceIds);
        methodManageService.remove(methodLqw);
        LambdaQueryWrapper<MethodModifyLog> modifyLog = new LambdaQueryWrapper<>();
        modifyLog.in(MethodModifyLog::getInterfaceId, interfaceIds);
        methodModifyLogService.remove(modifyLog);
        LambdaQueryWrapper<MethodVersionModifyLog> modifyVersionLog = new LambdaQueryWrapper<>();
        modifyVersionLog.in(MethodVersionModifyLog::getInterfaceId, interfaceIds);
        methodVersionModifyLogService.remove(modifyVersionLog);
        interfaceManageService.removeByIds(interfaceIds);
        LambdaQueryWrapper<InterfaceVersion> interfaceVersion = new LambdaQueryWrapper<>();
        interfaceVersion.in(InterfaceVersion::getInterfaceId, interfaceIds);
        versionService.remove(interfaceVersion);

    }

    public void clearAppAuthData(String appCode) {
        {
            LambdaQueryWrapper<HttpAuth> httpAuth = new LambdaQueryWrapper<>();
            httpAuth.eq(HttpAuth::getAppCode, appCode);
            httpAuthService.remove(httpAuth);
        }
        {
            LambdaQueryWrapper<HttpAuthDetail> httpAuthDetail = new LambdaQueryWrapper<>();
            httpAuthDetail.eq(HttpAuthDetail::getAppCode, appCode);
            httpAuthDetailService.remove(httpAuthDetail);
        }
        {
            LambdaQueryWrapper<HttpAuthApply> httpAuthApply = new LambdaQueryWrapper<>();
            httpAuthApply.eq(HttpAuthApply::getAppCode, appCode);
            httpAuthApplyService.remove(httpAuthApply);
        }
        {
            LambdaQueryWrapper<HttpAuthApplyDetail> httpAuthApplyDetail = new LambdaQueryWrapper<>();
            httpAuthApplyDetail.eq(HttpAuthApplyDetail::getAppCode, appCode);
            httpAuthApplyDetailService.remove(httpAuthApplyDetail);
        }
        {
            LambdaQueryWrapper<HttpAuthConfig> httpAuthConfig = new LambdaQueryWrapper<>();
            httpAuthConfig.eq(HttpAuthConfig::getAppCode, appCode);
            httpAuthConfigService.remove(httpAuthConfig);
        }
    }

    private RequestClient getClient(String cookie) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Cookie", "sso.jd.com=" + cookie);
        RequestClient requestClient = new RequestClient(JDDJ_BASE_URL, headers);
        return requestClient;
    }

    private Object getSuccessData(String result) {
        Map map = JsonUtils.parse(result, Map.class);
        if ("0".equals(map.get("code"))) {
            return map.get("result");
        } else {
            throw new BizException("获取数据失败：" + map.get("msg"));
        }
    }

    public Map<String, DjJoneAppInfo> getDjJoneAppInfo() {
        try {
            File file = ResourceUtils.getFile("classpath:data/importer/jddj.txt");
            String str = IOUtils.toString(new FileInputStream(file), "utf-8");
            String[] rows = StringUtils.split(str, "\r\n");
            List<DjJoneAppInfo> apps = new ArrayList<>();
            for (int i = 1; i < rows.length; i++) {// 跳过第一行
                String row = rows[i];
                String[] strs = StringUtils.split(row, '\t');
                DjJoneAppInfo appInfo = new DjJoneAppInfo();
                appInfo.setAppName(strs[0]);
                appInfo.setAppNameCn(strs[1]);
                appInfo.setAppLeader(strs[2]);
                appInfo.setAppLeaderNm(strs[3]);
                appInfo.setAppMembers(StringUtils.replace(strs[4], "\"", ""));
                appInfo.setAppMembersNm(StringUtils.replace(strs[5], "\"", ""));
                apps.add(appInfo);
            }
            Map<String, DjJoneAppInfo> ret = new HashMap<>();
            Map<String, List<DjJoneAppInfo>> map = apps.stream().collect(Collectors.groupingBy(DjJoneAppInfo::getAppName));
            for (Map.Entry<String, List<DjJoneAppInfo>> entry : map.entrySet()) {
                ret.put(entry.getKey(), entry.getValue().get(0));
            }
            return ret;
        } catch (Exception e) {
            throw StdException.adapt(e);
        }

    }

    public JddjResult<List<DjApiGroup>> getApiGroups(String cookie, String appCode, String env) {
        final RequestClient client = getClient(cookie);
        Map<String, Object> params = new HashMap<>();
        params.put("env", env);
        params.put("appCode", appCode);
        final String s = client.get(GROUP_GET_URL, params);
        return JsonUtils.parse(s, new TypeReference<JddjResult<List<DjApiGroup>>>() {
        });
    }

    /**
     * 将京东到家的app信息同步到当前系统
     */
    public void syncDjApp(String cookie) {
        final JddjResult<List<JddjApp>> jddjResult = getJddjApp(cookie, null);
        if (!jddjResult.isSuccess()) {
            log.error("jddj.err_get_app:result={}", JsonUtils.toJSONString(jddjResult));
            return;
        }
        List<AppInfo> appInfos = appInfoService.queryDjAppByPrefix(DJ_APP_PREFIX);
        Map<String, List<AppInfo>> appCode2Apps = appInfos.stream().collect(Collectors.groupingBy(AppInfo::getAppCode));
        Map<String, DjJoneAppInfo> djAppMap = getDjJoneAppInfo();
        for (JddjApp jddjApp : jddjResult.getResult()) {
            List<AppInfo> app = appCode2Apps.get(DJ_APP_PREFIX + jddjApp.getAppCode());
            Long id = null;
            if (!ObjectHelper.isEmpty(app)) {
                id = app.get(0).getId();
            }
            AppInfoDTO dto = new AppInfoDTO();
            dto.setId(id);
            dto.setAppCode(DJ_APP_PREFIX + jddjApp.getAppCode());
            dto.setAppName(jddjApp.getAppName());
            dto.setAuthLevel("0");
            DjJoneAppInfo djJoneAppInfo = djAppMap.get(jddjApp.getAppCode());
            if (djJoneAppInfo != null) {
                dto.setOwner(Collections.singletonList(djJoneAppInfo.getAppLeader()));
                String members = djJoneAppInfo.getAppMembers() + ",wangjingfang3";
                dto.setMember(StringHelper.split(members, ","));
                dto.setProductor(Collections.singletonList(djJoneAppInfo.getAppLeader()));
                dto.setTester(Collections.singletonList(djJoneAppInfo.getAppLeader()));
            } else {
                JagileMember appMember = jagileService.getAppMember(jddjApp.getAppCode());
                if ("1136351".equals(jddjApp.getAppCode())) continue;
                if (appMember != null) {
                    String productor = null;
                    if (!ObjectHelper.isEmpty(appMember.getAppOwner())) {
                        productor = appMember.getAppOwner().get(0);
                    } else if (!ObjectHelper.isEmpty(appMember.getAppAdmin())) {
                        productor = appMember.getAppAdmin().get(0);
                    } else if (!ObjectHelper.isEmpty(appMember.getSystemOwner())) {
                        productor = appMember.getSystemOwner().get(0);
                    }
                    dto.setOwner(appMember.getAppOwner());
                    dto.setTester(appMember.getAppTester());
                    dto.setTestMember(appMember.getSystemTester());
                    dto.setProductor(Collections.singletonList(productor));
                    Set<String> members = new HashSet<>();
                    members.add("wangjingfang3");
                    members.addAll(appMember.getSystemAdmin());
                    members.addAll(appMember.getAppAdmin());
                    members.addAll(appMember.getAppOp());
                    members.addAll(appMember.getUser());
                    members.addAll(appMember.getSystemOp());
                    members.addAll(appMember.getSystemOwner());
                    dto.setMember(members.stream().collect(Collectors.toList()));
                    if (ObjectHelper.isEmpty(dto.getOwner())) {
                        dto.setOwner(Collections.singletonList(productor));
                    }
                    if (ObjectHelper.isEmpty(dto.getTester())) {
                        dto.setTester(Collections.singletonList(productor));
                    }
                } else {
                    dto.setOwner(Collections.singletonList("wangjingfang3"));
                    dto.setTester(Collections.singletonList("wangjingfang3"));
                    dto.setProductor(Collections.singletonList("wangjingfang3"));
                }

            }
            AppInfoDTO cjgApp = cjgHelper.getCjgComponetInfoByCode(dto.getAppCode());
            if (cjgApp != null) {
                dto.setCjgAppId(cjgApp.getAppCode());
            }
            dto.setTenantId(UserSessionLocal.getUser().getTenantId());
            if (dto.getId() != null) {
                appInfoService.modifyApp(dto);
            } else {
                appInfoService.addApp(dto);
            }

        }
    }

    /**
     * @param cookie
     * @param appType jsf、http
     * @return
     */
    public JddjResult<List<JddjApp>> getJddjApp(String cookie, AppType appType) {
        RequestClient client = getClient(cookie);
        Map<String, Object> params = new HashMap<>();
        if (appType != null) {
            params.put("appType", appType.name());
        }
        String result = client.get(APP_GET_URL, params);
        JddjResult<List<JddjApp>> ret = JsonUtils.parse(result, new TypeReference<JddjResult<List<JddjApp>>>() {
        });
        return ret;
    }

    public void importJddjApp(String cookie, AppType appType, List<String> appCodes) {
        JddjResult<List<JddjApp>> result = getJddjApp(cookie, appType);
        List<AppInfo> appInfos = appInfoService.queryDjAppByPrefix(DJ_APP_PREFIX);
        Map<String, List<AppInfo>> code2Apps = appInfos.stream().collect(Collectors.groupingBy(AppInfo::getAppCode));
        if (result.isSuccess()) {
            for (JddjApp jddjApp : result.getResult()) {
                if (appCodes != null && !appCodes.isEmpty()) {
                    if (!appCodes.contains(jddjApp.getAppCode())) continue;
                }
                if (jddjApp.getEnvList().size() == 1) {
                    JddjDataImportTask task = new JddjDataImportTask();
                    task.setJddjApp(jddjApp);
                    task.setImporter(this);
                    task.setCookie(cookie);
                    task.setAppType(appType);
                    task.setDataSyncService(dataSyncService);
                    ImportDto importDto = new ImportDto();
                    importDto.setTargetAppCode(jddjApp.getAppCode());
                    importDto.setDjEnv(jddjApp.getEnvList().get(0));
                    importDto.setDjAppCode(jddjApp.getAppCode());
                    String targetAppCode = DJ_APP_PREFIX + jddjApp.getAppCode();
                    importDto.setTargetAppCode(targetAppCode);
                    importDto.setTargetAppSecret(code2Apps.get(targetAppCode).get(0).getAppSecret());
                    task.setDto(importDto);
                    scheduleService.execute(task);
                }
            }
        }
    }

    /**
     * 获取所有版本的app信息
     *
     * @param cookie
     * @param appCode
     * @param env
     * @param apiGroup
     * @return
     */
    public List<Map<String, Object>> getVersionInterfaces(String cookie, String appCode, String env, String apiGroup) {
        if (StringUtils.isBlank(apiGroup)) {
            JddjResult<List<DjApiGroup>> result = getApiGroups(cookie, appCode, env);
            if (!result.isSuccess()) {
                return Collections.emptyList();
            }
            List<Map<String, Object>> ret = new ArrayList<>();
            if (result.getResult() == null) return ret;
            for (DjApiGroup djApiGroup : result.getResult()) {
                String group = djApiGroup.getApiGroup();
                ret.addAll(getVersionInterfaces(cookie, appCode, env, group));
            }
            Collections.sort(ret, new Comparator<Map<String, Object>>() {
                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    long id1 = Variant.valueOf(o1.get("id")).toLong();
                    long id2 = Variant.valueOf(o1.get("id")).toLong();
                    return id1 > id2 ? 0 : 1;
                }
            });
            return ret;
        }
        RequestClient client = getClient(cookie);
        Map<String, Object> params = new HashMap<>();
        params.put("appCode", appCode);
        params.put("env", env);
        params.put("apiGroup", apiGroup);
        String result = client.get(API_SEARCH_URL, params);
        List<Map<String, Object>> data = (List<Map<String, Object>>) getSuccessData(result);
        /*List<String> ids = new ArrayList<>();
        for (Map<String, Object> datum : data) {
            ids.add(Variant.valueOf(datum.get("id")).toString());
        }*/
        return data;
    }

    private Map<String, Object> getSwagger(String cookie, String id) {
        RequestClient client = getClient(cookie);
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        String result = client.get(API_GET_URL, params);
        Map<String, Object> data = (Map<String, Object>) getSuccessData(result);
        return data;
    }

    @Transactional
    public Long importDjApp(String cookie, ImportDto dto, AppType appType) {
        AppInfo appInfo = appInfoService.findApp(dto.getTargetAppCode());
        if (!appInfo.getAppSecret().equals(dto.getTargetAppSecret())) {
            log.info("app.err_invalid_app:appCode={}", JsonUtils.toJSONString(dto));
            return null;
        }
        DataSyncRecord dataSyncRecord = dataSyncService.getLatestSyncRecord(dto.getDjAppCode(), dto.getDjEnv(), dto.getDjApiGroup());
        Long latestVersion = null;
        if (dataSyncRecord != null) {
            latestVersion = Variant.valueOf(dataSyncRecord.getLastSyncVersion()).toLong();
        }
        List<Map<String, Object>> versionInterfaces = getVersionInterfaces(cookie, dto.getDjAppCode(), dto.getDjEnv(), dto.getDjApiGroup());
        if (!versionInterfaces.isEmpty()) {
            Long newId = Variant.valueOf(versionInterfaces.get(0).get("id")).toLong();
            if (latestVersion != null && newId <= latestVersion) {
                log.info("app.ignore_already_processed_version:dto={}", dto);
                return null;
            }
        }
        for (int i = versionInterfaces.size() - 1; i >= 0; i--) {
            Map<String, Object> versionInterface = versionInterfaces.get(i);
            Object id = versionInterface.get("id");
            Long newId = Variant.valueOf(id).toLong();
            if (latestVersion != null && newId <= latestVersion) {
                log.info("app.ignore_already_processed_version:dto={},version={}", dto, newId);
                continue;
            }
            log.info("app.import_version_interface:id={},versionInterface={}", id, versionInterface);
            Map<String, Object> swagger = getSwagger(cookie, Variant.valueOf(id).toString());
            DocReportDto reportDto = new DocReportDto();
            reportDto.setHttpAppCode(dto.getDjAppCode());
            reportDto.setCreateDate((String) versionInterface.get("createTime"));
            Guard.notEmpty(appType, "要导入的应用类型不可为空");
            if (AppType.http.equals(appType)) {
                reportDto.setSwagger(JsonUtils.toJSONString(swagger));
            } else if (AppType.jsf.equals(appType)) {
                List<ClassMetadata> classMetadata = swaggerParserService.parseJsfMetadata(JsonUtils.toJSONString(swagger));
                reportDto.setJsfDocs(JsonUtils.toJSONString(classMetadata));
            }
            reportDto.setAppCode(dto.getTargetAppCode());
            reportDto.setAppSecret(dto.getTargetAppSecret());
            //docReportService.reportDoc(reportDto);
            docReportService.mergeReportDoc(appInfo, reportDto, null);
        }
        if (versionInterfaces.isEmpty()) return 0L;
        return Variant.valueOf(versionInterfaces.get(0).get("id")).toLong();
    }

    /**
     * 更新接口的部门信息
     */
    public void updateInterfaceDeptName() {
        // UserSessionLocal.getUser().getTenantId("10001");
        InterfaceAuthFilter filter = new InterfaceAuthFilter();
        filter.setSize(10000L);
        filter.setNullDept(true);
        // 查询JSF接口
        filter.setType(InterfaceTypeEnum.JSF.getCode());
        Page<InterfaceManage> jsfPage = interfaceManageService.pageMarketInterface(filter);
        // 查询HTTP接口
        filter.setType(InterfaceTypeEnum.HTTP.getCode());
        Page<InterfaceManage> httpPage = interfaceManageService.pageMarketInterface(filter);
        List<InterfaceManage> jsfList = jsfPage.getRecords();
        List<InterfaceManage> httpList = httpPage.getRecords();
        boolean b = jsfList.addAll(httpList);
        jsfList = jsfList.stream().filter(vs -> StringHelper.isBlank(vs.getDeptName())).collect(Collectors.toList());
        for (InterfaceManage interfaceManage : jsfList) {
            String deptName = interfaceManageService.getDeptName(interfaceManage.getAppId(), interfaceManage.getUserCode());
            interfaceManage.setDeptName(deptName);
        }
        int i = interfaceManageService.batchUpdateInterfaceDeptName(jsfList);
        System.out.println("update size is  " + i);
    }

    /*List<String> getApiInfos(){
        String url = "http://api.jddj.com/api/doc/search?appCode=o2o-crm-manager&env=pre1&apiGroup=swaggerDemoApi";
    }*/
    public static void main(String[] args) {
        JddjApiImporter importer = new JddjApiImporter();
        Map<String, DjJoneAppInfo> code2Apps = importer.getDjJoneAppInfo();
        String waitProcessedApps = "o2o-crm-manager, o2o-crm-query-service, o2o-crm-manager-yunying, myorder.o2o, O2O_PRICE_CORE, o2o_vender_settle_api, settle-new, o2o_oass, o2o-super-vip-user-api, o2o-super-vip-user, o2o-super-vip-user-manage-web, hw-brand, hw-brand-base, hw-brand-board, location.lsp.o2o, o2o-act-dataware-work, o2o-cms-api-work, o2o-trade-recommend-platform, ums.o2o, o2o-platform-customer-trade-assistant, yzt-promotion-offline-pre, yzt-promotion-offline, pdj_taro_orchard_static, yzt-auth, yzt-erp-open, yzt-stock-pre2, yzt-wms, yzt-print, yzt-data-middle-platform, pre_yzt_monitor, yzt_selection, yzt-selection-mainpre, o2o-promotion-forward-service, ordercenter.pdj.jd.local, mine.pdj.jd.local, order.pdj.jd.local, account.pdj.jd.local, pdj_m_login, o2o-web-product, o2o-platform-merchant-members-gateway, o2o-merchant-members, o2o-tech-portal-manage-service, o2o-message-center-service, pdj_service_home, account.o2o.jd.local, cart.o2o.web, 1136351, jm, pricecenter.service.o2o, O2O_PRICE_WEB, o2o-price-redline-web, o2o-coupon-core-web, o2o-coupon-core-inner, o2o-coupon-core-open, o2o-coupon-inner-api, o2o-coupon-merchant-web, o2o-coupon-open-api, o2o-coupon-show-open-api, o2o-coupon-trade, o2o-coupon-web, o2o_promotesku_btl_service, member-promotion-web, members-merchant-manage, o2o-promotion-price-open-api, jddj-promo-search-server, o2o-ocs-promise, o2o_promotion_manage_system, o2o_promotion_manage_system-web, o2o-pms-merchant-web, o2o-xapp-promotion-open, JddjPromoIndexAppService, JddjPromoIndexService, platformPromotionRuleengine-service, o2o-offiaccount-gateway, o2o-offiaccount-web, growth-common-service, o2o-vender-vip-benefit-app, o2o-search-model-sort, o2o-search-sort, o2o-comment-webGateway, o2o-athena-service, o2o-stockoutsku-follow-service, o2o-xapp-friendhelp-service, o2o-xapp-friendhelp-web, o2o-xapp-friendhelp-gateway, o2opromoteskuService, o2o_promotesku_lsp_web, freight_promote_server, afs-customer, o2o-zeus, o2o-pms-combo-api, o2o-pms-combo-gateway, o2o-pms-ext, o2o-pms-middle-yunying-web, business-operation-guidance-web, o2o-vender-self-member-jsf, o2o-vender-violation-service, addressweb, o2o-welfare-service, o2o-welfare-manage-web, o2o-skynet-web, o2o-heaven-server, o2o-heaven-manage-web, o2o-dict-open-web, o2o-lbs-core, hours-nearby-service, stockcenter.o2o, o2o-stock-marking, jddj_wechat_saf1, o2o-wechat-public-web, pdj_service, o2o.cart.pdj, o2o-api-platform-open-service, o2o-api-platform-manage-service, o2o-api-platform-manage-web, o2o-recommend-index, o2o-recommend-es-manager, o2o-recommend-recall, o2o-trade-recommend-repository, recommend-realtime-data, o2o-recommend-platform, o2o-trade-recommend-worker, o2o-trade-recommend-web, o2o-recommend-activity, o2o-trade-recommend-composite, o2o-recommend-store, o2orecommendchannel, o2o-recommend-channel, o2o-search-center-new, o2o-trade-search, new-product-marking-service, new-product-marking-build, afs-chengdu-worker, o2o-payment-api, o2o-payment-gateway, o2o.platform.promotion.planning.api, o2o.platform.promotion.planning.apply.web, o2o.platform.promotion.planning.yunying.web, o2o_user_member_service, o2o_user_member_webGateway, o2o_user_member_web, o2o-user-promotion-web, o2o-user-promotion-service, o2o-user-promotion-wg-web, o2o-user-promotion-operate-web, O2O-platform-businessCustomer-Center, o2o-cms-template-manage-work, o2o-short-url-open-service, o2o-short-url-trade, o2o-short-url-manager, o2o-cut-order-manager-web, o2o-cut-order-web, o2o-cut-order-open-service, o2o-giftcard-web, o2o-community-redpacket-trade, o2o-community-redpacket-web, new-vender-show, o2o-grab-sort-service, o2o-trade-commonservice, o2o-strategy-ai, o2o-strategy-trade, o2o-strategy-api, o2o-strategy-gateway, o2o-strategy-manage-web, o2o_promotion_manage_system_http, gw.login.o2o.jd.com, o2o-test-framework-manage-web, o2o-test-framework-manage-service, o2o-promotion-forward-open-service, o2o-promotion-forward-manage-web, o2o-platform-promotion-game-service, o2o-platform-promotion-game-web, o2o-platform-promotion-game-http, o2o-promotion-index-open-service, o2o-coupon-israfel-inner-api, o2o-coupon-israfel-open-api, o2o-coupon-israfel-web, o2o-subsidy-rule-web, orderdiscount-service, merchantOrderDiscount-web, orderdiscount-web, orderdiscount-core-service, order_remind_query_es, o2o-mobile-recharge-open-service, o2o-mobile-recharge-manage-web, jddj-cloud-web, o2o-darwin-mange-web, o2o-darwin-manage-api, o2o-freight-promotion-web, freight_promote_store_web, freight_promote_actitvity, o2o-freight-promotion-web-yunying, freight_promote_web, o2o-common-fission-jsf, o2o-dict-open-service, o2o-dict-manage-web, o2o-dict-manage-service, o2o-xapp-wallet-open-service, o2o-xapp-wallet-web, o2o-delivery-time, o2o-storeindex-baseinfo-service, o2o-storeindex-baseinfo-gateway, o2o-mlion-merchant-web, o2o-mlion-open-service, o2o-mlion-http, o2o-mlion-web, o2o-eagle-http, o2o-eagle-yunying-web, o2o-coupon-center-inner-api, o2o-grant-money-open-api, o2o-grant-money-trade, o2o-grant-money-web, rcs-rcc, firstorderdiscount-service, firstorderdiscount-merchant-web, firstorderdiscount-web";
        //System.out.println("appsInfos="+JsonUtils.toJSONString(apps));
        final String[] strs = StringUtils.split(waitProcessedApps, ",");
        List<String> notFound = new ArrayList<>();
        for (String str : strs) {
            if (!code2Apps.containsKey(str.trim())) {
                notFound.add(str);
            }
        }
        System.out.println("------------------------------------------------");
        System.out.println(notFound);
    }
}
