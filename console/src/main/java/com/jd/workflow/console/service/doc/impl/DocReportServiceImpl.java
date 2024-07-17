package com.jd.workflow.console.service.doc.impl;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.jim.cli.Cluster;
import com.jd.jsf.open.api.vo.InterfaceInfo;
import com.jd.jsf.open.api.vo.Result;
import com.jd.matrix.generic.spi.SPI;
import com.jd.matrix.generic.spi.func.SimpleSPIReducer;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.MethodTagEnum;
import com.jd.workflow.console.base.enums.SiteEnum;
import com.jd.workflow.console.dao.mapper.doc.AppDocReportRecordMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.app.AppAllInfo;
import com.jd.workflow.console.dto.doc.*;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.dto.group.RequirementTypeEnum;
import com.jd.workflow.console.dto.jingme.ButtonDTO;
import com.jd.workflow.console.dto.jingme.CustomDTO;
import com.jd.workflow.console.dto.jingme.TemplateMsgDTO;
import com.jd.workflow.console.dto.jingme.UserDTO;
import com.jd.workflow.console.dto.requirement.InterfaceSpaceDTO;
import com.jd.workflow.console.dto.test.jagile.DemandDetail;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.doc.AppDocReportRecord;
import com.jd.workflow.console.entity.doc.InterfaceVersion;
import com.jd.workflow.console.entity.doc.SyncJsfDocLog;
import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.entity.env.EnvConfigItem;
import com.jd.workflow.console.entity.model.ApiModel;
import com.jd.workflow.console.entity.model.ApiModelGroup;
import com.jd.workflow.console.entity.model.ApiModelTree;
import com.jd.workflow.console.entity.model.ModelRefRelation;
import com.jd.workflow.console.entity.parser.InterfaceInfoDown;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.fps.DataTypeCheckSPI;
import com.jd.workflow.console.fps.dto.DataType;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.helper.UserPrivilegeHelper;
import com.jd.workflow.console.model.sync.BuildReportContext;
import com.jd.workflow.console.model.sync.InterfaceJsonInfo;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.depend.InterfaceServiceWrap;
import com.jd.workflow.console.service.doc.IDocReportService;
import com.jd.workflow.console.service.doc.IInterfaceVersionService;
import com.jd.workflow.console.service.doc.SwaggerParserService;
import com.jd.workflow.console.service.doc.SyncJsfDocLogService;
import com.jd.workflow.console.service.env.impl.EnvConfigServiceImpl;
import com.jd.workflow.console.service.errorcode.IEnumPropService;
import com.jd.workflow.console.service.group.impl.RequirementGroupServiceImpl;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.console.service.jingme.SendMsgService;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.console.service.listener.impl.EasyMockInterfaceChangeListener;
import com.jd.workflow.console.service.measure.IMeasureDataService;
import com.jd.workflow.console.service.method.MethodModifyDeltaInfoService;
import com.jd.workflow.console.service.model.IApiModelGroupService;
import com.jd.workflow.console.service.model.IApiModelService;
import com.jd.workflow.console.service.model.IApiModelTreeService;
import com.jd.workflow.console.service.model.IModelRefRelationService;
import com.jd.workflow.console.service.parser.DocReportDtoBuilderService;
import com.jd.workflow.console.service.requirement.InterfaceSpaceService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.console.service.test.JagileRemoteCaller;
import com.jd.workflow.console.utils.DigestUtils;
import com.jd.workflow.console.utils.MethodDigest;
import com.jd.workflow.console.utils.RestTemplateUtils;
import com.jd.workflow.console.utils.VersionManager;
import com.jd.workflow.soap.common.Md5Utils;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.method.ClassMetadata;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DocReportServiceImpl extends ServiceImpl<AppDocReportRecordMapper, AppDocReportRecord> implements IDocReportService {
    static final String DEFAULT_HTTP_CODE = "defaultHttp";

    @Autowired
    SwaggerParserService swaggerParserService;
    @Autowired
    MethodManageServiceImpl methodManageService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    TransactionTemplate transactionTemplate;
    @Autowired
    List<InterfaceChangeListener> listeners = new ArrayList<>();
    @Resource(name = "jimClient")
    private Cluster jimClient;
    @Autowired
    MethodModifyDeltaInfoService deltaInfoService;
    @Autowired
    IHttpAuthDetailService httpAuthDetailService;
    @Autowired
    IInterfaceVersionService versionService;
    @Resource(name = "docThreadExecutor")
    ScheduledThreadPoolExecutor scheduleService;
    @Autowired
    EnvConfigServiceImpl envConfigService;
    @Resource(name = "defaultScheduledExecutor")
    ScheduledThreadPoolExecutor defaultScheduledExecutor;
    @Autowired
    IInterfaceMethodGroupService groupService;
    @Autowired
    IHttpAuthService cjgHttpAuthService;
    @Autowired
    private UserPrivilegeHelper userPrivilegeHelper;
    @Autowired
    IApiModelGroupService apiModelGroupService;
    @Autowired
    IApiModelService apiModelService;
    @Autowired
    IModelRefRelationService modelRefRelationService;
    @Autowired
    RefJsonTypeService refJsonTypeService;
    @Autowired
    IApiModelTreeService apiModelTreeService;
    @Autowired
    private IEnumPropService enumPropService;
    @Autowired
    private RequirementGroupServiceImpl requirementGroupService;
    @Autowired
    private RequirementInfoService requirementInfoService;
    @Autowired
    private IMemberRelationService relationService;
    @Autowired
    private InterfaceSpaceService interfaceSpaceService;
    @Autowired
    private IMeasureDataService measureDataService;
    @Autowired
    JagileRemoteCaller jagileRemoteCaller;
    @Autowired
    private SendMsgService sendMsgService;
    @Autowired
    private UserHelper userHelper;
    @Autowired
    private InterfaceServiceWrap interfaceServiceWrap;
    @Autowired
    private DocReportDtoBuilderService<DocReportDto> classMetaBuilder;
    @Autowired
    private DocReportDtoBuilderService<DocReportDto> modelsBuilder;
    @Autowired
    private DocReportDtoBuilderService<DocReportDto> appMetaBuilder;
    @Resource
    private RestTemplateUtils restTemplateUtils;
    @Autowired
    private SyncJsfDocLogService syncJsfDocLogService;


    private String getDocReportDigest(DocReportDto dto) {
        return Md5Utils.md5(dto.getJsfDocs() + "" + dto.getSwagger());
    }
    public boolean getLock(String key){
        boolean success = jimClient.setNX(key,"true");
        if(success){
            log.info("success_get_lock:key={}",key);
            // 最多缓存5分钟
            jimClient.expire(key,60*5, TimeUnit.SECONDS);
        }
        return success;
    }
    public void removeLock(String key){
        try{
            jimClient.del(key);
            log.info("success_remove_lock:key={}",key);
        }catch (Exception e){
            log.error("jimdb.err_remove_key:ky={}",key,e);
        }

    }
    @Override
    public void reportDocHashPrivilege(DocReportDto dto) {
        if (ObjectHelper.isEmpty(dto.getImportData())) {
            return;
        }
        AppInfo appInfo = appInfoService.findApp(dto.getAppCode());
        Guard.notEmpty(appInfo, "无效的app:" + dto.getAppCode());
        dto.setAppSecret(appInfo.getAppSecret());
        InterfaceManage manage = interfaceManageService.getOneById(dto.getInterfaceId());
        Guard.notEmpty(appInfo, "无效的接口:" + dto.getInterfaceId());
        dto.setHttpAppCode(manage.getServiceCode());
        saveAppDocReportRecord(dto);
        if (dto.getImportData() != null) {
            List<GroupHttpData<MethodManage>> group2MethodManage = new ArrayList<>();

            for (Map.Entry<String, List<MethodManage>> entry : dto.getImportData().entrySet()) {
                GroupHttpData<MethodManage> httpData = new GroupHttpData<>();

                httpData.setGroupDesc(entry.getKey());
                httpData.setHttpData(entry.getValue());
                group2MethodManage.add(httpData);
            }

            List<MethodManage> existMethods = methodManageService.getInterfaceMethods(manage.getId());
            DocUpdateData docUpdateData = mergeMethod(manage, group2MethodManage, existMethods, "path", dto);
            processAuthKey(docUpdateData.getAddAndUpdated());
        }
    }

    private void saveAppDocReportRecord(DocReportDto dto) {
        AppDocReportRecord record = new AppDocReportRecord();
        record.setReportTime(new Date());
        record.setAppCode(dto.getAppCode());
        record.setHttpAppCode(dto.getHttpAppCode());
        record.setIp(dto.getIp());
        record.setDigest(getDocReportDigest(dto));
        record.setCodeRepository(dto.getCodeRepository());
        record.setBranch(dto.getBranch());
        record.setChannel(dto.getChannel());
        if (StringUtils.isNotEmpty(dto.getErp())) {
            record.setCreator(dto.getErp());
        }
        save(record);
    }

    public Long reportDoc(DocReportDto dto, boolean sync) {
        String lockKey = "japi_report_doc_"+dto.getAppCode();

        if (dto.getHttpAppCode() == null) {
            dto.setHttpAppCode(dto.getAppCode() + DEFAULT_HTTP_CODE);
        }
        AppInfo appInfo = appInfoService.findApp(dto.getAppCode());
        Guard.notEmpty(appInfo, "无效的app:" + dto.getAppCode());

        if (!appInfo.getAppSecret().equals(dto.getAppSecret())) {
            throw new BizException("无效的app密钥：" + dto.getAppSecret());
        }

                boolean hasLock = getLock(lockKey);
                if (!hasLock) {
                    throw new BizException("请勿重复上报（当前上报未处理完成）");
                }
                try {
                    if (StringUtils.isNotBlank(dto.getJdosAppCode())) {
                        if (!dto.getJdosAppCode().equals(appInfo.getJdosAppCode())) {
                            appInfo.setJdosAppCode(dto.getJdosAppCode());
                            appInfoService.updateById(appInfo);
                        }
                    }
                    saveAppDocReportRecord(dto);
                    GroupResolveDto groupResolveDto = new GroupResolveDto();
                    if (StringUtils.isNotEmpty(dto.getRequireCode())) {
                        getSpace(dto.getRequireCode(), dto.getErp(), dto.getMembers(), groupResolveDto);
                    }
                    if (sync) {
                        try {
                            mergeReportDoc(appInfo, dto, groupResolveDto);
                            return groupResolveDto.getId();
                        } catch (Exception e) {
                            log.error("docReport.err_merge_report_doc:appName={},appInfo={},ip={}", appInfo.getAppName(), appInfo, dto.getIp());
                            return groupResolveDto.getId();
                        } finally {
                            removeLock(lockKey);
                        }
                    } else {
                        scheduleService.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    mergeReportDoc(appInfo, dto, groupResolveDto);
                                } catch (Exception e) {
                                    log.error("docReport.err_merge_report_doc:appName={},appInfo={},ip={}", appInfo.getAppName(), appInfo, dto.getIp());
                                } finally {
                                    removeLock(lockKey);
                                }

                            }
                        });
                        return groupResolveDto.getId();
                        }
                    } catch(Exception e){
                        removeLock(lockKey);
                        throw e;
                    }


    }

            @Override
            public Long reportDoc (DocReportDto dto){
                return reportDoc(dto, dto.getSync() != null && dto.getSync());
            }

            public Long reportDocFromJsfPlatform (DocReportDto dto){
                String jdosAppCode = dto.getAppCode().replace("J-dos-", "");
                AppInfo appInfo = appInfoService.InitByJdosCode(jdosAppCode);
                if (Objects.isNull(appInfo)) {
                    log.error("DocReportServiceImpl.reportDocFromJsfPlatform 创建Japi 应用失败");
                    throw new RuntimeException("创建Japi 应用失败" + dto.getJdosAppCode());
                }
                dto.setAppSecret(appInfo.getAppSecret());
                //设置密钥上报文档
                return reportDoc(dto);
            }

            @Override
            public boolean reportJavaBean (JavaBeanReportDto dto){
                AppInfo appInfo = appInfoService.findApp(dto.getAppCode());
                Guard.notEmpty(appInfo, "无效的app:" + dto.getAppCode());

                if (!appInfo.getAppSecret().equals(dto.getAppSecret())) {
                    throw new BizException("无效的app密钥：" + dto.getAppSecret());
                }
                interfaceManageService.saveJavaBean(dto.getBeanInfos(), appInfo.getId());
                return true;
            }

            @Override
            public AppAllInfo loadAppInfo (String appCode, String appSecret, String ip){
                AppInfo appInfo = appInfoService.findApp(appCode);
                Guard.notEmpty(appInfo, "无效的app:" + appCode);

                if (!appInfo.getAppSecret().equals(appSecret)) {
                    throw new BizException("无效的app密钥：" + appSecret);
                }
                AppAllInfo appAllInfo = new AppAllInfo();
                appAllInfo.setAppId(appInfo.getId());
                appAllInfo.setHttpApis(new ArrayList<>());
                appAllInfo.setJsfInterfaces(new ArrayList<>());
                appAllInfo.setEnvInfos(new ArrayList<>());

                List<InterfaceManage> appInterfaces = interfaceManageService.getAppInterfaces(appInfo.getId());
                List<Future> futures = new ArrayList<>();
                if (StringUtils.isNotBlank(ip)) {
                    String envName = "本地环境" + ip;
                    List<EnvConfig> envConfigList = envConfigService.getEnvConfigList(appInfo.getId(), null, null);
                    Optional<EnvConfig> optionalEnvConfig = envConfigList.stream().filter(item -> envName.equals(item.getEnvName())).findAny();
                    EnvConfig envConfig = null;
                    if (!optionalEnvConfig.isPresent()) {
                        envConfig = envConfigService.initLocalEnvConfig(appInfo, ip, envName);
                        envConfigList.add(envConfig);
                    } else {
                        envConfig = optionalEnvConfig.get();
                    }
                    List<EnvConfigItem> envConfigItems = envConfigService.getEnvConfigItems(envConfig.getId());
                    appAllInfo.setLocalEnvName(envName);
                    if (!envConfigItems.isEmpty()) {
                        appAllInfo.setLocalEnvAddress(envConfigItems.get(0).getUrl());
                    }

                }

                for (InterfaceManage appInterface : appInterfaces) {


                    Future future = defaultScheduledExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            List<MethodManage> methods = methodManageService.getInterfaceMethods(appInterface.getId());


                            if (InterfaceTypeEnum.HTTP.getCode().equals(appInterface.getType())) {
                                for (MethodManage method : methods) {
                                    AppAllInfo.HttpMethodInfo httpMethodInfo = new AppAllInfo.HttpMethodInfo();
                                    httpMethodInfo.setId(method.getId());
                                    httpMethodInfo.setHttpMethod(method.getHttpMethod());
                                    httpMethodInfo.setPath(method.getPath());
                                    httpMethodInfo.setMethodCode(method.getMethodCode());
                                    httpMethodInfo.setName(method.getName());
                                    appAllInfo.getHttpApis().add(httpMethodInfo);
                                }

                            } else if (InterfaceTypeEnum.JSF.getCode().equals(appInterface.getType())) {
                                ClassMetadata classMetadata = new ClassMetadata();
                                classMetadata.setClassName(appInterface.getServiceCode());
                                for (MethodManage method : methods) {
                                    MethodMetadata methodMetadata = new MethodMetadata();
                                    methodMetadata.setId(method.getId());
                                    methodMetadata.setMethodName(method.getMethodCode());
                                    classMetadata.getMethods().add(methodMetadata);
                                }
                                appAllInfo.getJsfInterfaces().add(classMetadata);
                            }

                        }
                    });
                    futures.add(future);
                }
                for (Future future : futures) {
                    try {
                        future.get();
                    } catch (Exception e) {
                        throw new BizException("获取接口失败", e);
                    }
                }
                return appAllInfo;
            }

            @Override
            public void addListener (InterfaceChangeListener interfaceChangeListener){
                listeners.add(interfaceChangeListener);
            }


            @Override
            public Map<String, List<MethodManage>> parseFile (MultipartFile file){
                DataType dataType = new DataType();
                dataType.setFile(file);
                String result = SPI.of(DataTypeCheckSPI.class, spi -> spi.checkData(dataType))
                        .filter(spec -> spec.getGroup().equals("file"))
                        .reduce(SimpleSPIReducer::first)
                        .call();
                log.info("app.fetch_cjg_app_result={}", result);
                try {
                    List<GroupHttpData<MethodManage>> groupHttpData = swaggerParserService.parseSwagger(result);
                    Map<String, List<MethodManage>> group2MethodManage = groupHttpData.stream().collect(Collectors.toMap(GroupHttpData::getGroupDesc, GroupHttpData::getHttpData));
                    return group2MethodManage;
                } catch (Exception e) {
                    log.info("swagger.err_parse_swagger", e);
                    throw new BizException("文件不符合swagger规范", e);
                }
            }

            @Override
            public Map<String, List<MethodManage>> parseUrl (String url){
                DataType dataType = new DataType();
                dataType.setUrl(url);
                String result = SPI.of(DataTypeCheckSPI.class, spi -> spi.checkData(dataType))
                        .filter(spec -> spec.getGroup().equals("url"))
                        .reduce(SimpleSPIReducer::first)
                        .call();
                log.info("app.fetch_cjg_app_result={}", result);
                try {
                    List<GroupHttpData<MethodManage>> groupHttpData = swaggerParserService.parseSwagger(result);
                    Map<String, List<MethodManage>> group2MethodManage = groupHttpData.stream().collect(Collectors.toMap(GroupHttpData::getGroupDesc, GroupHttpData::getHttpData));
                    return group2MethodManage;
                } catch (Exception e) {
                    throw new BizException("文件不符合swagger规范");
                }
            }

            @Override
            public boolean reportEnums (EnumsReportDTO dto){
                AppInfo appInfo = appInfoService.findApp(dto.getAppCode());
                Guard.notEmpty(appInfo, "无效的app:" + dto.getAppCode());

                if (!appInfo.getAppSecret().equals(dto.getAppSecret())) {
                    throw new BizException("无效的app密钥：" + dto.getAppSecret());
                }
                enumPropService.saveEnums(dto.getEnums(), appInfo.getId());
                return true;

            }


    @Override
    public void syncDocFromJsfPlatform(InterfaceJsonInfo interfaceJsonInfo) {
        int totalNo = 0, doWhileNo = 0;
        Boolean flag = true;
        try {
            do {
                Result<List<InterfaceInfo>> interfaceListResult = interfaceServiceWrap.searchInterface(interfaceJsonInfo.getInterfaceClassFullName(), interfaceJsonInfo.getPageSize(), interfaceJsonInfo.getStartNo());
                doBatchInterfaceList(interfaceListResult);
                totalNo = interfaceListResult.getTotal();
                interfaceJsonInfo.setStartNo(interfaceJsonInfo.getStartNo() + interfaceJsonInfo.getPageSize());
                if (flag) {
                    break;
                }
                doWhileNo = doWhileNo + 1;
                log.info("DocReportServiceImpl.syncDocFromJsfPlatform startNo"+interfaceJsonInfo.getStartNo());
            } while (interfaceJsonInfo.getStartNo() < totalNo && doWhileNo <= interfaceJsonInfo.getMaxWhileNo());
        } catch (Throwable e) {
            log.error("DocReportServiceImpl.syncDocFromJsfPlatform Exception ", e);
        }
    }

    public void syncDocFromJsfPlatformWorkerT1() {
        int totalNo = 0, doWhileNo = 0, startNo = 0, pageSize = 30;
        String interfaceName = "com";
        Boolean continueFlag = true;
        try {
            do {
                Result<List<InterfaceInfo>> interfaceListResult = interfaceServiceWrap.searchInterface(interfaceName, pageSize, startNo);
                if(CollectionUtils.isNotEmpty(interfaceListResult.getData())){
                    doBatchInterfaceList(interfaceListResult);
                    InterfaceInfo lastInterfaceInfo = interfaceListResult.getData().get(interfaceListResult.getData().size() - 1);
                    LocalDateTime dataTime = lastInterfaceInfo.getCreatedTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                    LocalDateTime now = LocalDate.now().atStartOfDay();
                    LocalDateTime previousDay = now.minusDays(1);
                    continueFlag = previousDay.isBefore(dataTime);
                }
                totalNo = interfaceListResult.getTotal();
                startNo = startNo + pageSize;
                doWhileNo = doWhileNo + 1;
                log.info("DocReportServiceImpl.syncDocFromJsfPlatform startNo"+totalNo);
            } while (startNo < totalNo && doWhileNo <= 10000 && continueFlag);
        } catch (Throwable e) {
            log.error("DocReportServiceImpl.syncDocFromJsfPlatform Exception ", e);
        }
    }

    /**
     * 执行批量接口导入，并把结果入表
     * @param interfaceListResult
     */
    private void doBatchInterfaceList(Result<List<InterfaceInfo>> interfaceListResult) {
        try {
            List<SyncJsfDocLog> syncJsfDocLogs = new ArrayList<>();
            for (InterfaceInfo interfaceInfo : interfaceListResult.getData()) {
                String result = null;
                if (interfaceInfo.getProviderLive() > 0) {
                    result = doSyncDocFromJsfPlatform(interfaceInfo);
                } else {
                    result = "无provider 不处理";
                }
                SyncJsfDocLog syncJsfDocLog = getSyncJsfDocLog(interfaceInfo, result);
                syncJsfDocLogs.add(syncJsfDocLog);
            }
            syncJsfDocLogService.saveBatch(syncJsfDocLogs);
        } catch (Throwable e) {
            log.error("DocReportServiceImp.doBatchInterfaceList Exception ",e);
        }
    }

    /**
     * 获取执行日志记录
     * @param interfaceInfo
     * @param result
     * @return
     */
    private SyncJsfDocLog getSyncJsfDocLog(InterfaceInfo interfaceInfo, String result) {
        SyncJsfDocLog syncJsfDocLog = new SyncJsfDocLog();
        syncJsfDocLog.setInterfaceName(interfaceInfo.getInterfaceName());
        if("添加成功".equals(result)){
            syncJsfDocLog.setStatus(1);
        }
        syncJsfDocLog.setRemart(result);
        return syncJsfDocLog;
    }

    private String doSyncDocFromJsfPlatform(InterfaceInfo interfaceInfo) {
        String resultStr = "添加成功";
        try {
            //已存在接口不处理
            List<InterfaceManage> interfaceManages = interfaceManageService.geInterfaceByName(interfaceInfo.getInterfaceName());
            if (CollectionUtils.isNotEmpty(interfaceManages)) {
                log.info("DocReportServiceImpl.syncDocFromJsfPlatform 接口以上报 {}", interfaceInfo.getInterfaceName());
                resultStr = "接口已存在";
                return resultStr;
            }
            BuildReportContext buildReportContext = new BuildReportContext();
            buildReportContext.setInterfaceInfo(interfaceInfo);
            //处理应用信息
            DocReportDto reportDto = appMetaBuilder.build(new DocReportDto(), buildReportContext);
            //处理类信息
            reportDto = classMetaBuilder.build(reportDto, buildReportContext);
            //处理model数据
            reportDto = modelsBuilder.build(reportDto, buildReportContext);
//            postHttp(reportDto);
            reportDocFromJsfPlatform(reportDto);
        } catch (Throwable e) {
            log.error("DocReportServiceImpl.doSyncDocFromJsfPlatform Exception", e);
            resultStr = "Exception " + e.getMessage();
        }
        return resultStr;

    }

    public void postHttp(DocReportDto reportDto) {
        HttpHeaders headers = new HttpHeaders();
//        headers.add("Host", hotUpdateEnvironmentConf.getHost());
        headers.setContentType(MediaType.APPLICATION_JSON);
        String url = "http://beta-data-flow.jd.com/doc/reportDocFromJsfPlatform";
        log.info("remoteHotDeploy url:{},header:{},body:{}", url, JSON.toJSONString(headers), JSON.toJSONString(reportDto));
        String response = restTemplateUtils.post(url, headers, JSON.toJSONString(reportDto));
        log.info("remoteHotDeploy response:{}", response);
    }


            /**
             * 处理方法鉴权标识
             *
             * @param methods
             */
            public void processAuthKey (List < MethodManage > methods) {
                try {
                    log.info("#processAuthKey.methods={}", JsonUtils.toJSONString(methods));
                    if (CollectionUtils.isEmpty(methods)) {
                        return;
                    }
                    List<HttpAuthDTO> authList = new ArrayList<>();
                    //<authCode , HttpAuthDTO>
                    Map<String, HttpAuthDTO> authMap = new HashMap<>();
                    List<HttpAuthDetailDTO> authDetailList = new ArrayList<>();
                    //<appCode_path , HttpAuthDetailDTO>
                    Map<String, HttpAuthDetailDTO> authDetailMap = new HashMap<>();
                    //<interfaceId , InterfaceManage>
                    Map<Long, InterfaceManage> interfaceMap = new HashMap<>();
                    AppInfo appInfo = null;
                    for (MethodManage method : methods) {
                        //判断是否存在鉴权标识
                        List<String> authCodeList = method.getAuthKey();
                        if (CollectionUtils.isEmpty(authCodeList)) {
                            continue;
                        }
                        InterfaceManage interfaceManage = interfaceMap.get(method.getInterfaceId());
                        if (interfaceManage == null) {
                            interfaceManage = interfaceManageService.getById(method.getInterfaceId());
                        }
                        if (interfaceManage != null) {
                            interfaceMap.put(method.getInterfaceId(), interfaceManage);
                        }
                        if (appInfo == null) {
                            appInfo = appInfoService.getById(interfaceManage.getAppId());
                            if (appInfo == null) {
                                continue;
                            }
                        }

                        //获取鉴权列表
                        getAuthList(authList, authMap, authCodeList, interfaceManage, appInfo);

                        //获取鉴权明细列表
                        getAuthDetailList(authDetailList, authDetailMap, method, authCodeList, interfaceManage, appInfo);
                    }

                    log.info("#processAuthKey.authDetailList={}", JsonUtils.toJSONString(authDetailList));
                    log.info("#processAuthKey.authList={}", JsonUtils.toJSONString(authList));
                    log.info("#processAuthKey.appInfo={}", JsonUtils.toJSONString(appInfo));
                    MethodManage method = methods.get(0);
                    cjgHttpAuthService.reportHttpAuth(authList, authDetailList, appInfo, method.getInterfaceId());
                } catch (Exception e) {
                    log.error("doc.err_process_auth_key_data", e);
                }


            }

            /**
             * 获取鉴权明细列表
             *
             * @param authDetailList
             * @param authDetailMap
             * @param method
             * @param authCodeList
             * @param interfaceManage
             */
            private void getAuthDetailList
            (List < HttpAuthDetailDTO > authDetailList, Map < String, HttpAuthDetailDTO > authDetailMap,
                    MethodManage method, List < String > authCodeList, InterfaceManage interfaceManage, AppInfo appInfo)
            {
                String appCode = appInfo.getAppCode();
                String appName = appInfo.getAppName();
                String path = method.getPath();
                Long methodId = method.getId();
                for (String authCode : authCodeList) {
                    // key = 应用编码_接口id_鉴权标识_path
                    String key = appCode + "_" + methodId + "_" + authCode + "_" + path;
                    if (authDetailMap.get(key) != null) {
                        continue;
                    }
                    HttpAuthDetailDTO authDetailDTO = new HttpAuthDetailDTO();
                    authDetailDTO.setAppCode(appCode);
                    authDetailDTO.setAppName(appName);
                    authDetailDTO.setSite(SiteEnum.China.getCode());
                    authDetailDTO.setAuthCode(authCode);
                    authDetailDTO.setCreated(new Date());
                    authDetailDTO.setModified(new Date());
                    authDetailDTO.setYn(DataYnEnum.VALID.getCode());
                    authDetailDTO.setCreator("docReport");
                    authDetailDTO.setModifier("docReport");
                    authDetailDTO.setInterfaceId(method.getInterfaceId());
                    authDetailDTO.setInterfaceCode(interfaceManage.getServiceCode());
                    authDetailDTO.setInterfaceName(interfaceManage.getName());
                    authDetailDTO.setMethodId(method.getId());
                    authDetailDTO.setMethodCode(method.getMethodCode());
                    authDetailDTO.setMethodName(method.getName());
                    authDetailDTO.setPath(method.getPath());
                    authDetailList.add(authDetailDTO);
                    authDetailMap.put(key, authDetailDTO);
                }
            }

            /**
             * 获取鉴权标识列表
             *
             * @param authList
             * @param authMap
             * @param authCodeList
             * @param interfaceManage
             */
            private void getAuthList (List < HttpAuthDTO > authList, Map < String, HttpAuthDTO > authMap,
                    List < String > authCodeList, InterfaceManage interfaceManage, AppInfo appInfo){
                String appCode = appInfo.getAppCode();
                String appName = appInfo.getAppName();
                for (String authCode : authCodeList) {
                    String key = appCode + "_" + authCode;
                    if (authMap.get(key) != null) {
                        continue;
                    }

                    HttpAuthDTO authDTO = new HttpAuthDTO();
                    authDTO.setAppCode(appCode);
                    authDTO.setAppName(appName);
                    authDTO.setSite(SiteEnum.China.getCode());
                    authDTO.setAuthCode(authCode);
                    authDTO.setCreated(new Date());
                    authDTO.setModified(new Date());
                    authDTO.setYn(DataYnEnum.VALID.getCode());
                    authDTO.setCreator("docReport");
                    authDTO.setModifier("docReport");
                    authList.add(authDTO);
                    authMap.put(key, authDTO);
                }
            }

            public void collectAllGroups (Long appId, String pkgName, List < ApiModelGroup > groups){
                if (StringHelper.isEmpty(pkgName)) return;
                ApiModelGroup group = new ApiModelGroup();
                group.setFullName(pkgName);
                group.setAppId(appId);
                String name = StringHelper.lastPart(pkgName, '.');
                group.setName(name);
                group.setEnName(name);
                group.setYn(1);
                groups.add(group);
                if (pkgName.indexOf(".") != -1) {
                    String parentPkgName = pkgName.substring(0, pkgName.lastIndexOf("."));
                    collectAllGroups(appId, parentPkgName, groups);
                }
            }

            private Map<String, ApiModelGroup> collectAllGroup (Long appId, List < ApiClassModel > classModels){
                Map<String, ApiModelGroup> pkgs = new HashMap<>();
                for (ApiClassModel classModel : classModels) {
                    int index = classModel.getClassName().lastIndexOf(".");
                    if (index != -1) {
                        String pkgName = classModel.getClassName().substring(0, index);
                        if (pkgs.containsKey(pkgName)) continue;
                        List<ApiModelGroup> apiModelGroups = new ArrayList<>();
                        collectAllGroups(appId, pkgName, apiModelGroups);
                        for (ApiModelGroup entry : apiModelGroups) {
                            if (pkgs.containsKey(entry.getFullName())) continue;
                            pkgs.put(entry.getFullName(), entry);
                        }

                    }
                }
                return pkgs;
            }

            public void mergeModels (AppInfo appInfo, List < ApiClassModel > classModels){
                log.info("doc.report_models:appId={},appInfo={}", appInfo.getAppName(), appInfo);
                try {
                    if (CollectionUtils.isEmpty(classModels)) {
                        return;
                    }
                    List<ApiModel> existModels = apiModelService.getModelsByAppId(appInfo.getId());
                    log.info("api.found_exist_models:existModels={},newModels={}", existModels.stream().map(item -> item.getName()).collect(Collectors.joining(",")), classModels);
                    List<ApiModelGroup> existGroups = apiModelGroupService.getGroupsByAppId(appInfo.getId());

                    Map<String, ApiModelGroup> newGroups = collectAllGroup(appInfo.getId(), classModels);
                    List<ApiModel> newModels = new ArrayList<>();
                    for (ApiClassModel classModel : classModels) {
                        ApiModel newModel = new ApiModel();
                        newModel.setAppId(appInfo.getId());
                        newModel.setYn(1);
                        newModel.setName(classModel.getClassName());
                        newModel.setAutoReport(1);
                        newModel.setCreated(new Date());
                        JsonType content = classModel.getModel();
                        newModel.setContent(content);
                        newModel.setRefNames(refJsonTypeService.collectRefNames(content));
                        newModel.setDigest(DigestUtils.getJsonTypeDigest(content));
                        newModels.add(newModel);
                    }
                    List<ApiModel> savedOrUpdatedModels = mergeModelByDigest(existModels, newModels);
                    ApiModelTree tree = mergeGroups(appInfo, existGroups, newGroups);
                    for (ApiModel savedModel : savedOrUpdatedModels) {
                        MethodSortModel sortModel = new MethodSortModel();
                        sortModel.setId(savedModel.getId());
                        sortModel.setName(savedModel.getName());
                        int index = savedModel.getName().lastIndexOf('.');
                        if (index != -1) {
                            String pkgName = savedModel.getName().substring(0, index);
                            GroupSortModel group = tree.getTreeModel().findGroup(pkgName);
                            if (group.getChildren() == null) {
                                group.setChildren(new ArrayList<>());
                            }
                            group.getChildren().add(sortModel);
                        } else {

                            tree.getTreeModel().getTreeItems().add(sortModel);
                        }
                    }
                    tree.getTreeModel().removeDuplicated();
                    clearGroupName(tree.getTreeModel().getTreeItems());
                    apiModelTreeService.updateById(tree);


                } catch (Exception e) {
                    log.error("doc.err_report_models:appId={},appInfo={}", appInfo.getAppName(), appInfo, e);
                }
            }

            private void clearGroupName (List < TreeSortModel > treeItems) {
                if (treeItems == null) return;
                for (TreeSortModel treeItem : treeItems) {
                    if (treeItem instanceof GroupSortModel) {
                        treeItem.setName(null);
                        clearGroupName(((GroupSortModel) treeItem).getChildren());
                    }
                }
            }

            private ApiModelTree mergeGroups (AppInfo
            appInfo, List < ApiModelGroup > existGroups, Map < String, ApiModelGroup > newGroups){
                for (ApiModelGroup existGroup : existGroups) {
                    if (newGroups.containsKey(existGroup.getFullName())) {
                        newGroups.remove(existGroup.getFullName());
                    }
                }

                ApiModelTree tree = apiModelTreeService.getTreeByAppId(appInfo.getId());
                if (tree == null) {
                    tree = new ApiModelTree();
                    tree.setAppId(appInfo.getId());
                    tree.setTreeModel(new MethodGroupTreeModel());
                    tree.setYn(1);
                    apiModelTreeService.save(tree);
                }
                MethodGroupTreeModel treeModel = tree.getTreeModel();
                if (treeModel == null) {
                    treeModel = new MethodGroupTreeModel();
                    tree.setTreeModel(treeModel);
                }
                for (ApiModelGroup existGroup : existGroups) {
                    GroupSortModel group = treeModel.findGroup(existGroup.getId());
                    if (group != null) {
                        group.setName(existGroup.getFullName());
                    }
                }
                if (!newGroups.isEmpty()) {
                    apiModelGroupService.saveBatch(newGroups.values());
                }
                for (Map.Entry<String, ApiModelGroup> entry : newGroups.entrySet()) {
                    insertGroup(treeModel, newGroups, entry.getValue());
                }

                return tree;
            }

            private String getParentPkgName (String pkgName){
                int index = pkgName.lastIndexOf(".");
                if (index == -1) return null;
                return pkgName.substring(0, index);
            }

            public void insertGroup (MethodGroupTreeModel
            treeModel, Map < String, ApiModelGroup > newGroups, ApiModelGroup group){
                GroupSortModel existGroup = treeModel.findGroup(group.getFullName());
                if (existGroup != null) return;
                GroupSortModel groupSortModel = new GroupSortModel();
                groupSortModel.setName(group.getFullName());
                groupSortModel.setId(group.getId());

                GroupSortModel parent = null;
                int index = group.getFullName().lastIndexOf(".");
                if (index == -1) {
                    parent = new GroupSortModel();
                    parent.setChildren(treeModel.getTreeItems());
                } else {
                    String parentName = group.getFullName().substring(0, index);
                    String originalParentName = parentName;

                    while (true) {
                        group = newGroups.get(parentName);
                        if (group != null) {// 是新分组
                            insertGroup(treeModel, newGroups, group);
                            parentName = getParentPkgName(parentName);
                            if (parentName == null) { //到
                                break;
                            } else {

                            }
                        } else {
                            break;
                        }

                    }
                    parent = treeModel.findGroup(originalParentName);
                }
                parent.getChildren().add(groupSortModel);
            }


            private List<ApiModel> mergeModelByDigest (List < ApiModel > existModels, List < ApiModel > newModels){
                Map<String, ApiModel> name2ExistModel = new HashMap<>();
                for (ApiModel existModel : existModels) {
                    name2ExistModel.put(existModel.getName(), existModel);
                }
                List<String> addedModelNames = new ArrayList<>();
                List<String> updatedModelNames = new ArrayList<>();
                List<ApiModel> savedModels = new ArrayList<>();
                List<ApiModel> updatedModels = new ArrayList<>();
                List<ApiModel> unChanged = new ArrayList<>();
                RefJsonTypeService service = new RefJsonTypeService();
                for (ApiModel newModel : newModels) {
                    ApiModel existModel = name2ExistModel.get(newModel.getName());
                    if (existModel != null) {
                        newModel.setId(existModel.getId());
                        if (!newModel.getDigest().equals(existModel.getDigest())) {
                            newModel.setAutoReport(1);
                            newModel.setCreated(existModel.getCreated());
                            newModel.setRefNames(service.collectRefNames(newModel.getContent()));
                            modelRefRelationService.merge(existModel.getRefNames(), newModel.getRefNames(), existModel.getAppId(), existModel.getId(), ModelRefRelation.TYPE_MODEL);
                            updatedModelNames.add(newModel.getName());
                            updatedModels.add(newModel);
                            log.info("api.update_model:model={}", newModel);
                            apiModelService.updateById(newModel);
                        } else {
                            unChanged.add(newModel);
                        }
                    } else {
                        savedModels.add(newModel);
                        newModel.setRefNames(service.collectRefNames(newModel.getContent()));
                        addedModelNames.add(newModel.getName());
                    }
                }
                if (!savedModels.isEmpty()) {
                    log.info("api.save_models:models={}", savedModels.stream().map(item -> item.getName()).collect(Collectors.joining(",")));
                    apiModelService.saveBatch(savedModels);
                    for (ApiModel savedModel : savedModels) {
                        modelRefRelationService.merge(null, savedModel.getRefNames(), savedModel.getAppId(), savedModel.getId(), ModelRefRelation.TYPE_MODEL);
                    }
                }
                log.info("doc.merge_models:added={},updated={}", addedModelNames, updatedModelNames);
                List<ApiModel> savedOrUpdatedModels = new ArrayList<>();
                savedOrUpdatedModels.addAll(savedModels);
                savedOrUpdatedModels.addAll(updatedModels);
                savedOrUpdatedModels.addAll(unChanged);
                return savedOrUpdatedModels;
            }


            public void mergeReportDoc (AppInfo appInfo, DocReportDto dto, GroupResolveDto groupResolveDto){
                if (dto.getErp() != null) {
                    UserInfoInSession session = new UserInfoInSession();
                    session.setUserId(dto.getErp());
                    session.setUserName(dto.getErp());
                    UserSessionLocal.setUser(session);
                } else {
                    UserSessionLocal.setUser(new UserInfoInSession());
                }
                try {
                    log.info("doc.report_doc:appId={},appInfo={},ip={}", appInfo.getAppName(), appInfo, dto.getIp());
                    mergeModels(appInfo, dto.getModels());
                    List<MethodManage> methodManageList = Lists.newArrayList();
                    StringBuilder contentBuilder = new StringBuilder();
                    try {
                        List<MethodManage> httpMethodList = mergeHttpDoc(appInfo, dto);
                        methodManageList.addAll(httpMethodList);
                        if (CollectionUtils.isNotEmpty(httpMethodList)) {
                            contentBuilder.append("此次上报新增变更http接口数：" + httpMethodList.size() + ";");
                        }

                    } catch (Exception e) {
                        log.error("doc.err_report_http_doc:appId={},appInfo={},ip={}", appInfo.getAppName(), appInfo, dto.getIp(), e);
                    }
                    try {
                        List<MethodManage> jsfMethodList = mergeJsfDoc(appInfo, dto);
                        methodManageList.addAll(jsfMethodList);
                        if (CollectionUtils.isNotEmpty(jsfMethodList)) {
                            contentBuilder.append("此次上报新增变更jsf接口数：" + jsfMethodList.size() + ";");
                        }
                    } catch (Exception e) {
                        log.error("doc.err_report_jsf_doc:appId={},appInfo={},ip={}", appInfo.getAppName(), appInfo, dto.getIp(), e);
                    }
                    try {

                        InitRequirement(methodManageList, appInfo, dto.getRequireCode(), dto.getErp(), dto.getMembers(), groupResolveDto);

                    } catch (Exception e) {
                        log.error("doc.err_init_requirement:appId={},appInfo={},ip={}", appInfo.getAppName(), appInfo, dto.getIp(), e);
                    }
                    sendJingMeMsg(appInfo, dto, groupResolveDto, contentBuilder.toString());
                } finally {
                    UserSessionLocal.removeUser();
                }
            }

            private void sendJingMeMsg (AppInfo appInfo, DocReportDto dto, GroupResolveDto groupResolveDto, String
            content){
                if (StringUtils.isEmpty(dto.getMembers())) {
                    return;
                }
                String[] memberList = dto.getMembers().split(",");

                TemplateMsgDTO templateMsgDTO = new TemplateMsgDTO();
                templateMsgDTO.setHead("接口文档变更通知");
                templateMsgDTO.setSubHeading("上报了接口");
                if (StringUtils.isNotEmpty(dto.getErp())) {
                    UserVo userVo = userHelper.getUserBaseInfoByUserName(dto.getErp());
                    UserDTO userDTO = new UserDTO();
                    userDTO.setTenantCode("ee");
                    userDTO.setErp(dto.getErp());
                    userDTO.setRealName(userVo.getRealName());
                    List<UserDTO> userDTOList = Lists.newArrayList();
                    userDTOList.add(userDTO);
                    templateMsgDTO.setAtUsers(userDTOList);

                }
                templateMsgDTO.setContent(content);

                List<CustomDTO> customDTOList = Lists.newArrayList();
                CustomDTO customDTO = new CustomDTO();
                customDTO.setName("所属应用：");
                customDTO.setDescription(appInfo.getAppName());
                customDTOList.add(customDTO);

                if (StringUtils.isNotEmpty(dto.getRequireCode())) {
                    String erp = dto.getErp();
                    if(StringUtils.isBlank(erp)){
                        erp = memberList[0];
                    }

                    CommonResult<DemandDetail> detail = jagileRemoteCaller.getDemandByCode(dto.getRequireCode(),erp);
                    CustomDTO custom2DTO = new CustomDTO();
                    custom2DTO.setName("所属需求：");
                    custom2DTO.setDescription(detail.getData().getName());
                    customDTOList.add(custom2DTO);
                }

                templateMsgDTO.setCustomFields(customDTOList);
                List<ButtonDTO> buttonDTOList = Lists.newArrayList();
                ButtonDTO appBtn = new ButtonDTO();
                appBtn.setName("查看应用详情");
                appBtn.setPcUrl(String.format("http://console.paas.jd.com/idt/fe-app-view/demandManage/%s", appInfo.getId()));
                buttonDTOList.add(appBtn);
                if (StringUtils.isNotEmpty(dto.getRequireCode())) {
                    ButtonDTO spaceBtn = new ButtonDTO();
                    spaceBtn.setName("查看需求详情");
                    spaceBtn.setPcUrl(String.format("http://console.paas.jd.com/idt/fe-demand-view/demandManage/%s", groupResolveDto.getId()));
                    buttonDTOList.add(spaceBtn);
                }

                templateMsgDTO.setButtons(buttonDTOList);

                for (String erp : memberList) {
                    sendMsgService.sendUserJueMsg(erp, templateMsgDTO);
                }


            }

            private void InitRequirement (List < MethodManage > methodManageList, AppInfo appInfo, String
            requireCode, String owner, String members, GroupResolveDto dto){
                if (StringUtils.isEmpty(requireCode)) {
                    return;
                }
                if (CollectionUtils.isEmpty(methodManageList)) {
                    return;
                }
                if (StringUtils.isEmpty(owner)) {
                    return;
                }
                if (Objects.isNull(dto)) {
                    return;
                }

                dto.setType(2);


                List<InterfaceSortModel> models = Lists.newArrayList();
                MethodGroupTreeDTO methodGroupTreeDTO = groupService.findAppHttpTree(appInfo.getId());
                getExistMethodGroupTree(methodManageList, models, methodGroupTreeDTO);
                methodGroupTreeDTO = groupService.findAppJsfTree(appInfo.getId());
                getExistMethodGroupTree(methodManageList, models, methodGroupTreeDTO);

                requirementGroupService.appendGroupTreeModel(dto, null, models);
            }

            private void getSpace (String requireCode, String owner, String members, GroupResolveDto dto){
                LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
                lqw.eq(RequirementInfo::getRelatedRequirementCode, requireCode);
                lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
                lqw.eq(RequirementInfo::getYn, 1);
                List<RequirementInfo> requirementInfoList = requirementInfoService.list(lqw);
                //已存在需求，就把人员添加进去
                if (CollectionUtils.isNotEmpty(requirementInfoList)) {
                    dto.setId(requirementInfoList.get(0).getId());
                    List<MemberRelation> saveList = Lists.newArrayList();
                    List<MemberRelation> memberRelationList = requirementInfoService.getMembers(requirementInfoList.get(0).getId());
                    Set<String> erpMap = memberRelationList.stream().map(MemberRelation::getUserCode).collect(Collectors.toSet());
                    if (StringUtils.isNotEmpty(members)) {
                        String[] memberList = members.split(",");
                        for (String erp : memberList) {
                            if (!erpMap.contains(erp)) {
                                erpMap.add(erp);
                                saveList.add(requirementInfoService.newMemberRelation(erp, requirementInfoList.get(0).getId()));
                            }
                        }
                    }
                    if (!erpMap.contains(owner)) {
                        saveList.add(requirementInfoService.newMemberRelation(owner, requirementInfoList.get(0).getId()));
                    }
                    if (CollectionUtils.isNotEmpty(saveList)) {
                        relationService.saveBatch(saveList);
                    }


                } else {
                    //不存在需求，就新建一个需求空间
                    InterfaceSpaceDTO interfaceSpaceDTO = new InterfaceSpaceDTO();

                    interfaceSpaceDTO.setCode(requireCode);
                    interfaceSpaceDTO.setOwner(owner);
                    if (StringUtils.isNotEmpty(members)) {
                        String[] memberList = members.split(",");
                        interfaceSpaceDTO.setMembers(Arrays.asList(memberList));
                    }
                    interfaceSpaceDTO.setName(owner + "创建的需求空间");
                    String erp = owner;
                    CommonResult<DemandDetail> detail = jagileRemoteCaller.getDemandByCode(requireCode,erp);

                    if (null != detail.getData()) {
                        interfaceSpaceDTO.setName(detail.getData().getName());
                    }

                    Long interfaceId = interfaceSpaceService.createSpace(interfaceSpaceDTO);

                    dto.setId(interfaceId);
                }
            }

            private void getExistMethodGroupTree
            (List < MethodManage > methodManageList, List < InterfaceSortModel > models, MethodGroupTreeDTO
            methodGroupTreeDTO){
                Set<Long> methodIdSet = methodManageList.stream().map(MethodManage::getId).collect(Collectors.toSet());
                for (MethodSortModel methodSortModel : methodGroupTreeDTO.getTreeModel().allMethods()) {
                    if (!methodIdSet.contains(methodSortModel.getId())) {
                        methodGroupTreeDTO.getTreeModel().removeMethod(methodSortModel.getId(), null);
                    }
                }
                methodGroupTreeDTO.getTreeModel().removeEmptyGroup(methodGroupTreeDTO.getTreeModel().getTreeItems());


                for (TreeSortModel treeItem : methodGroupTreeDTO.getTreeModel().getTreeItems()) {
                    if (treeItem instanceof InterfaceSortModel) {
                        models.add((InterfaceSortModel) treeItem);
                    }
                }
            }


            InterfaceManage getOrNewHttpInterface (AppInfo appInfo, DocReportDto dto){
                String deptName = appInfoService.getDeptNameFoyAppMember(appInfo);
                InterfaceManage interfaceManage = interfaceManageService.getAppInterface("" + appInfo.getId(), dto.getHttpAppCode(), false);
                if (interfaceManage == null) {
                    interfaceManage = new InterfaceManage();
                    interfaceManage.setType(InterfaceTypeEnum.HTTP.getCode());
                    interfaceManage.setAutoReport(1);
                    interfaceManage.setAppId(appInfo.getId());
                    interfaceManage.setTenantId(appInfo.getTenantId());
                    interfaceManage.setServiceCode(dto.getHttpAppCode());
                    interfaceManage.setName(dto.getHttpAppCode());
                    if (StringUtils.isNotBlank(deptName)) {
                        interfaceManage.setDeptName(deptName);
                    }
                    interfaceManageService.save(interfaceManage);
           /* InterfaceVersion interfaceVersion = versionService.initInterfaceVersion(interfaceManage);
            interfaceManage.setLatestDocVersion(interfaceVersion.getVersion());
            interfaceManageService.updateById(interfaceManage);*/
                }
                if (StringUtils.isNotBlank(deptName)) {
                    interfaceManage.setDeptName(deptName);
                }
                interfaceManage.setAutoReport(1);
                return interfaceManage;
            }

            /**
             * 上报接口的时候，有可能上报的方法数据是局部的，这个时候，局部的方法数据需要与现有的方法数据做一个合并操作
             *
             * @param appCode
             * @param interfaceId
             * @param existMethods
             * @param docUpdateData
             * @return
             */
            private List<MethodManage> buildAllAuthData (String appCode, Long
            interfaceId, List < MethodManage > existMethods, DocUpdateData docUpdateData){
                List<MethodManage> result = new ArrayList<>();

                QueryHttpAuthDetailReqDTO queryAuthDetailDTO = new QueryHttpAuthDetailReqDTO();
                queryAuthDetailDTO.setAppCode(appCode);
                queryAuthDetailDTO.setSite(SiteEnum.China.getCode()); //默认主站
                queryAuthDetailDTO.setInterfaceId(interfaceId);
                List<HttpAuthDetail> authDetailList = httpAuthDetailService.queryAllSourceList(queryAuthDetailDTO);
                Map<Long, List<HttpAuthDetail>> methodId2AuthDetail = authDetailList.stream().collect(Collectors.groupingBy(HttpAuthDetail::getMethodId));
                for (MethodManage existMethod : existMethods) { // 设置已有方法的authKey
                    List<HttpAuthDetail> httpAuthDetails = methodId2AuthDetail.get(existMethod.getId());
                    List<String> existAuthKeys = new ArrayList<>();
                    if (httpAuthDetails != null) {
                        //existMethod.setAuthKey(httpAuthDetails.stream().map(vs -> vs.getAuthCode()).collect(Collectors.toList()));
                        existAuthKeys = httpAuthDetails.stream().map(vs -> vs.getAuthCode()).collect(Collectors.toList());
                    }
                    MethodManage updatedMethod = docUpdateData.getUpdatedMethod(existMethod.getId());
                    if (updatedMethod != null) {
                        existMethod.setAuthKey(updatedMethod.getAuthKey());
                        result.add(updatedMethod);
                    } else {
                        existMethod.setAuthKey(existAuthKeys);
                        result.add(existMethod);
                    }
                }

                result.addAll(docUpdateData.getAdded());
                return result;
            }

            public List<MethodManage> mergeHttpDoc (AppInfo appInfo, DocReportDto dto){ //
                dto.init();
                if (StringUtils.isBlank(dto.getSwagger()) && ObjectHelper.isEmpty(dto.getGroupHttpData())
                        && ObjectHelper.isEmpty(dto.getHttpData()) && ObjectHelper.isEmpty(dto.getImportData())) {
                    return Collections.emptyList();
                }
                if (dto.getHttpAppCode() == null) {
                    dto.setHttpAppCode(dto.getAppCode() + DEFAULT_HTTP_CODE);
                }


                InterfaceManage manage = getOrNewHttpInterface(appInfo, dto);
                List<GroupHttpData<MethodManage>> group2MethodManage = new ArrayList<>();
                if (!StringUtils.isBlank(dto.getSwagger())) { // 全量上报接口文档
                    group2MethodManage = swaggerParserService.parseSwagger(dto.getSwagger());
                    List<MethodManage> existMethods = methodManageService.getInterfaceMethods(manage.getId());
                    DocUpdateData docUpdateData = mergeMethod(manage, group2MethodManage, existMethods, "path", dto);
                    processAuthKey(docUpdateData.getAddAndUpdated());
                    // 【指标度量】保存上报记录
                    measureDataService.saveReportDataLog(manage.getType(), group2MethodManage, dto.getErp());
                    return docUpdateData.getAddAndUpdated();
                } else if (dto.getGroupHttpData() != null) { // 上报局部的接口文档，处理authKey数据的时候，需要先将旧数据合并过来
                    for (GroupHttpData<HttpMethodModel> groupHttpDatum : dto.getGroupHttpData()) {
                        List<MethodManage> methodManages = groupHttpDatum.getHttpData().stream().map(httpMethodModel -> {
                            return SwaggerParserService.fromHttpMethodModel(httpMethodModel);
                        }).collect(Collectors.toList());
                        GroupHttpData<MethodManage> group = new GroupHttpData<>();
                        group.setGroupDesc(groupHttpDatum.getGroupDesc());
                        group.setGroupName(groupHttpDatum.getGroupName());
                        group.setHttpData(methodManages);
                        group2MethodManage.add(group);
                    }
                    // 【指标度量】保存上报记录
                    measureDataService.saveReportDataLog(manage.getType(), group2MethodManage, dto.getErp());
                    return reportPartData(manage, group2MethodManage, appInfo, dto);
                } else {
                    return Collections.emptyList();
                }
            }

            private List<MethodManage> reportPartData (InterfaceManage manage,
                    List < GroupHttpData < MethodManage >> group2MethodManage,
                    AppInfo appInfo,
                    DocReportDto dto){
                List<MethodManage> existMethods = methodManageService.getInterfaceMethods(manage.getId());
                DocUpdateData docUpdateData = mergeMethod(manage, group2MethodManage, existMethods, "path", dto);
                List<MethodManage> allMethodAuthData = buildAllAuthData(appInfo.getAppCode(), manage.getId(), existMethods, docUpdateData);
                processAuthKey(allMethodAuthData);
                return docUpdateData.getAddAndUpdated();
            }

            public List<MethodManage> mergeJsfDoc (AppInfo appInfo, DocReportDto dto){
                List<MethodManage> methodManageList = Lists.newArrayList();
                String deptName = appInfoService.getDeptNameFoyAppMember(appInfo);
                String jsfDocs = dto.getJsfDocs();
                if (StringUtils.isBlank(jsfDocs)) return methodManageList;
                List<ClassMetadata> jsfInterfaces = JsonUtils.parseArray(jsfDocs, ClassMetadata.class);
                if (jsfInterfaces == null) return methodManageList;
                for (ClassMetadata classMetadata : jsfInterfaces) {
                    InterfaceManage jsfInterface = null;
                    try {
                        jsfInterface = interfaceManageService.getAppInterface("" + appInfo.getId(), classMetadata.getClassName(), false);
                        if (jsfInterface == null) {
                            jsfInterface = new InterfaceManage();
                            jsfInterface.setPath(classMetadata.getPomPath());
                            jsfInterface.setType(InterfaceTypeEnum.JSF.getCode());
                            if (StringUtils.isNotBlank(classMetadata.getDesc())) {
                                jsfInterface.setName(SwaggerParserService.truncateStr(classMetadata.getDesc(), 256));
                            } else {
                                jsfInterface.setName(classMetadata.getClassName());
                            }

                            jsfInterface.setAppId(appInfo.getId());
                            if (Objects.nonNull(dto.getAutoReport())) {
                                jsfInterface.setAutoReport(dto.getAutoReport());
                            } else {
                                jsfInterface.setAutoReport(1);
                            }
                            jsfInterface.setTenantId(appInfo.getTenantId());
                            jsfInterface.setServiceCode(classMetadata.getClassName());
                            if (StringUtils.isNotBlank(deptName)) {
                                jsfInterface.setDeptName(deptName);
                            }
                            interfaceManageService.save(jsfInterface);
                            jsfInterface.setVisibility(0);
                            for (InterfaceChangeListener listener : listeners) {
                                if(listener instanceof EasyMockInterfaceChangeListener) {
                                    continue;
                                }
                                listener.onInterfaceAdd(Collections.singletonList(jsfInterface));
                            }
                    /*InterfaceVersion interfaceVersion = versionService.initInterfaceVersion(jsfInterface);
                    jsfInterface.setLatestDocVersion(interfaceVersion.getVersion());
                    interfaceManageService.updateById(jsfInterface);*/
                        } else {
                            boolean hasUpdate = false;
                            if (StringUtils.isNotBlank(classMetadata.getDesc())) {
                                jsfInterface.setName(SwaggerParserService.truncateStr(classMetadata.getDesc(), 256));
                                hasUpdate = true;
                            }

                            if (StringUtils.isNotBlank(classMetadata.getPomPath())
                                    && !classMetadata.getPomPath().contains("${") // 处理 com.jd.workflow:flow-client:${version} 这种情况
                            ) {
                                jsfInterface.setPath(classMetadata.getPomPath());
                                hasUpdate = true;
                            }
                            if (hasUpdate) {
                                interfaceManageService.updateById(jsfInterface);
                            }


                        }
                        if (StringUtils.isNotBlank(deptName)) {
                            jsfInterface.setDeptName(deptName);
                        }
                        List<MethodManage> newMethods = new ArrayList<>();
                        for (MethodMetadata method : classMetadata.getMethods()) {
                            newMethods.add(toMethodMetadata(method));
                        }
                        List<MethodManage> existMethods = methodManageService.getInterfaceMethods(jsfInterface.getId());

                        List<GroupHttpData<MethodManage>> group2MethodManage = new ArrayList<>();
                        GroupHttpData<MethodManage> groupHttpData = new GroupHttpData<>();
                        groupHttpData.setGroupName("default");
                        groupHttpData.setGroupDesc("default");
                        groupHttpData.setHttpData(newMethods);
                        group2MethodManage.add(groupHttpData);
                        DocUpdateData docUpdateData = mergeMethod(jsfInterface, group2MethodManage, existMethods, "methodCode", dto);
                        methodManageList.addAll(docUpdateData.getAddAndUpdated());

                        // 【指标度量】保存上报记录
                        measureDataService.saveReportDataLog(jsfInterface.getType(), group2MethodManage, dto.getErp());
                    } catch (Exception e) {
                        log.error("doc.err_report_jsf_doc:interfaceId={},jsfInterface={}", jsfInterface.getId(), classMetadata.getClassName(), e);
                    }

                }
                return methodManageList;
            }

            private MethodManage toMethodMetadata (MethodMetadata metadata){
                MethodManage methodManage = new MethodManage();
                String name = metadata.getCnName();
       /* if(StringUtils.isEmpty(name)){
            name = metadata.getDesc();
            if(StringUtils.isEmpty(name)){
                name = metadata.getMethodName();
            }
        }*/
                methodManage.setName(SwaggerParserService.truncateStr(metadata.getDesc(), 128));

                methodManage.setType(InterfaceTypeEnum.JSF.getCode());
                methodManage.setMethodCode(metadata.getMethodName());
                methodManage.setDesc(SwaggerParserService.truncateStr(metadata.getDesc(), 256));
                methodManage.setDocInfo(metadata.getDesc());
                methodManage.setDigest(DigestUtils.getJsfMethodMd5(methodManage, metadata));
                methodManage.setContent(JsonUtils.toJSONString(metadata));
                methodManage.setMethodTag(StringUtils.isNotBlank(metadata.getFunctionId()) ? MethodTagEnum.COLOR.getCode() : 0);
                methodManage.setFunctionId(metadata.getFunctionId());
                return methodManage;
            }

            private Map<String, List<MethodManage>> groupBy (List < MethodManage > methods, String field){
                Map<String, List<MethodManage>> path2Methods = new HashMap<>();
                for (MethodManage method : methods) {
                    String keyValue = (String) BeanTool.getProp(method, field);
                    List<MethodManage> existMethods = path2Methods.computeIfAbsent(keyValue, vs -> new ArrayList<>());
                    existMethods.add(method);

                }
                return path2Methods;
            }

            /**
             * 新方法的分组与旧方法保持一致即可
             *
             * @return
             */
            private GroupSortModel findNewMethodGroup (MethodGroupTreeModel treeModel,
                    List < MethodManage > newMethods
                    , Map < String, List < MethodManage >> existMethods, String keyField, String groupName){

                if (treeModel == null || treeModel.getTreeItems().isEmpty()) {
                    return null;
                }

                for (MethodManage newMethod : newMethods) { //未找到任何方法
                    String keyValue = (String) BeanTool.getProp(newMethod, keyField);
                    List<MethodManage> existMethodList = existMethods.get(keyValue);
                    if (ObjectHelper.isEmpty(existMethodList)) continue;
                    MethodManage existMethod = existMethodList.get(0);
                    GroupSortModel methodParent = treeModel.findMethodParent(existMethod.getId());
                    if (methodParent != null) return methodParent;
                }

                return treeModel.findGroup(groupName);
            }

            /**
             * 构造新的方法分组
             *
             * @param interfaceManage
             * @param groupName2Methods
             * @param existMethods
             * @param keyField
             */
            private void mergeNewMethodGroup (InterfaceManage
            interfaceManage, List < GroupHttpData < MethodManage >> groupName2Methods
                    , Map < String, List < MethodManage >> existMethods, String keyField){


                boolean interfaceHasUpdate = false;
                Map<InterfaceMethodGroup, GroupSortModel> methodGroupMap = new HashMap<>();
                List<InterfaceMethodGroup> newGroups = new ArrayList<>();
                List<InterfaceMethodGroup> modifyGroups = new ArrayList<>();
                if (interfaceManage.getSortGroupTree() == null
                ) {
                    MethodGroupTreeModel treeModel = new MethodGroupTreeModel();
                    interfaceManage.setSortGroupTree(treeModel);

                    interfaceHasUpdate = true;
                }
                interfaceManage.setGroupLastVersion(DateUtil.getCurrentDateMillTime());
                for (GroupHttpData<MethodManage> methodGroup : groupName2Methods) {
                    String groupDesc = methodGroup.getGroupDesc();
                    List<MethodManage> methods = methodGroup.getHttpData();
                    GroupSortModel newMethodGroup = findNewMethodGroup(interfaceManage.getSortGroupTree(), methods, existMethods, keyField, groupDesc);
                    if (newMethodGroup == null) { // 所有的方法都是新方法
                        GroupSortModel sortModel = new GroupSortModel();
                        sortModel.setName(groupDesc);
                        sortModel.setEnName(methodGroup.getGroupName());
                        InterfaceMethodGroup interfaceMethodGroup = new InterfaceMethodGroup();
                        interfaceMethodGroup.setType(GroupTypeEnum.APP.getCode());
                        interfaceMethodGroup.setInterfaceId(interfaceManage.getId());
                        interfaceMethodGroup.setYn(1);
                        interfaceMethodGroup.setName(SwaggerParserService.truncateStr(groupDesc, 256));
                        newGroups.add(interfaceMethodGroup);

                        methodGroupMap.put(interfaceMethodGroup, sortModel);
                        interfaceManage.getSortGroupTree().getTreeItems().add(sortModel);

                        for (MethodManage methodManage : methods) {
                            sortModel.getChildren().add(new MethodSortModel(methodManage.getId()));
                        }
                    } else { // 只有一部分方法是新方法，那么新方法的分组设置到老分组上
                        boolean isAllMethodInGroup = isAllMethodInGroup(newMethodGroup, methods); // 所有方法都在一个分组里时，需要修改分组名称
                        if (isAllMethodInGroup && (
                                !groupDesc.equals(newMethodGroup.getName())
                                        || !ObjectHelper.equals(newMethodGroup.getEnName(), methodGroup.getGroupName())
                        )

                        ) {
                            InterfaceMethodGroup group = new InterfaceMethodGroup();
                            group.setId(newMethodGroup.getId());
                            group.setName(SwaggerParserService.truncateStr(groupDesc, 256));
                            group.setEnName(swaggerParserService.truncateStr(methodGroup.getGroupName(), 100));
                            group.setInterfaceId(interfaceManage.getId());
                            group.setType(GroupTypeEnum.APP.getCode());
                            modifyGroups.add(group);
                        }
                        for (MethodManage newMethod : methods) {
                            String keyValue = (String) BeanTool.getProp(newMethod, keyField);
                            List<MethodManage> existMethodList = existMethods.get(keyValue);
                            if (ObjectHelper.isEmpty(existMethodList)) {
                                interfaceHasUpdate = true;
                                if (newMethodGroup.getChildren() == null) {
                                    newMethodGroup.setChildren(new ArrayList<>());
                                }
                                newMethodGroup.getChildren().add(new MethodSortModel(newMethod.getId()));
                            }

                        }
                    }
                }
                if (!newGroups.isEmpty()) {
                    groupService.saveBatch(newGroups);
                    for (Map.Entry<InterfaceMethodGroup, GroupSortModel> entry : methodGroupMap.entrySet()) {
                        entry.getValue().setId(entry.getKey().getId());
                    }
                }
                if (!modifyGroups.isEmpty()) {
                    for (InterfaceMethodGroup modifyGroup : modifyGroups) {
                        groupService.updateById(modifyGroup);
                    }
                }
                if (interfaceHasUpdate) { // 后面统一更新接口版本
                    //interfaceManageService.updateById(interfaceManage);
                }

            }

            boolean isAllMethodInGroup (GroupSortModel group, List < MethodManage > methods){
                return methods.stream().filter(vs -> vs.getId() != null).allMatch(item -> {
                    List<TreeSortModel> match = group.getChildren().stream().filter(child -> {
                        return child instanceof TreeSortModel && child.getId().equals(item.getId());
                    }).collect(Collectors.toList());
                    return !match.isEmpty();
                });
            }

            /**
             * 根据menthod 对象中的path 值找到已经删除的method
             *
             * @param group2MethodManage
             * @param existMethods
             * @param keyField
             * @return java.util.List<com.jd.workflow.console.entity.MethodManage>
             * @author wufagang
             * @date 2023/5/4 10:28
             */
            private List<MethodManage> getRemovedMethods (List < GroupHttpData < MethodManage >> group2MethodManage,
                    List < MethodManage > existMethods, String keyField){
                Set<String> keys = new HashSet<>();
                for (GroupHttpData<MethodManage> group : group2MethodManage) {
                    for (MethodManage newMethod : group.getHttpData()) {
                        String keyValue = (String) BeanTool.getProp(newMethod, keyField);
                        keys.add(keyValue);
                    }
                }

                List<MethodManage> removed = new ArrayList<>();
                for (MethodManage existMethod : existMethods) {
                    String keyValue = (String) BeanTool.getProp(existMethod, keyField);
                    if (!keys.contains(keyValue)) {
                        removed.add(existMethod);
                    }
                }
                return removed;
            }

            /**
             * 自动上报的接口暂时不删除
             *
             * @param manage             接口对象
             * @param group2MethodManage 新传入数据
             * @param existMethods       接口下已经存在的方法数据
             * @param keyField           path｜methodCode 枚举值
             */
            public DocUpdateData mergeMethod (InterfaceManage            manage, List < GroupHttpData < MethodManage >> group2MethodManage,
                    List < MethodManage > existMethods, String keyField, DocReportDto dto
    ){
                // 有些接口文档没有任何变化，但是authKey可能有变更，需要记录下来
                List<MethodManage> authKeyUpdated = new ArrayList<>();

                MethodGroupTreeDTO methodGroupTree = groupService.findMethodGroupTree(manage.getId());
                manage.setSortGroupTree(methodGroupTree.getTreeModel());

                List<MethodManage> newAdded = new ArrayList<>();
                List<MethodManage> updated = new ArrayList<>();
                List<MethodManage> removed = getRemovedMethods(group2MethodManage, existMethods, keyField);

                List<MethodManage> structedUpdated = new ArrayList<>();// 发生结构变更的方法
                List<MethodManage> updatedBeforeMethods = new ArrayList<>();// 发生结构变更的方法之前的值
                Map<String, List<MethodManage>> path2Methods = groupBy(existMethods, keyField);

                // 新创建方法的分组与老方法保持一致即可
                for (GroupHttpData<MethodManage> group : group2MethodManage) {
                    List<MethodManage> methods = group.getHttpData();
                    Map<String, List<MethodManage>> newPath2Methods = groupBy(methods, keyField);

                    for (MethodManage newMethod : methods) {
                        newMethod.setInterfaceId(manage.getId());
                        String keyValue = (String) BeanTool.getProp(newMethod, keyField);
                        List<MethodManage> existMethodList = path2Methods.get(keyValue);

                        List<MethodManage> sameKeyMethods = newPath2Methods.get(keyValue);

                        if (ObjectHelper.isEmpty(existMethodList)) {
                            newAdded.add(newMethod);
                        } else {
                            MethodManage existMethod = null;
                            if (existMethodList.size() == 1 && sameKeyMethods.size() == 1) { // 当前路径对应的方法只有一个
                                existMethod = existMethodList.get(0);
                            } else { // 当前路径下对应的方法有多个，需要比较http方法是否一致
                                for (MethodManage methodManage : existMethodList) {
                                    if (ObjectHelper.equals(newMethod.getHttpMethod(), methodManage.getHttpMethod())) {
                                        existMethod = methodManage;
                                        break;
                                    }
                                }
                            }
                            if (existMethod == null) {
                                newAdded.add(newMethod);
                                continue;
                            } else {
                                newMethod.setId(existMethod.getId());
                            }
                            if (/*existMethod.getReportSyncStatus() != null
                            && existMethod.getReportSyncStatus() != 0
                            ||*/ ObjectHelper.equals(existMethod.getDigest(), newMethod.getDigest())
                            ) { // 去掉reportSyncStatus的逻辑，因为现在会自动判别是否有变更
                                existMethod.setAuthKey(newMethod.getAuthKey());
                                authKeyUpdated.add(existMethod);
                                continue;
                            }


                            newMethod.setId(existMethod.getId());
                            updated.add(newMethod);
                            updatedBeforeMethods.add(existMethod);
                            if (MethodDigest.parse(newMethod.getDigest()).structHasUpdate(existMethod.getDigest())) {
                                structedUpdated.add(newMethod);

                            }
                        }

                    }

                }

                authKeyUpdated.addAll(updated);
                MethodGroupTreeModel groupTreeModel = groupService.findMethodGroupTree(manage.getId()).getTreeModel();
                methodManageService.saveBatch(newAdded);

                for (InterfaceChangeListener listener : listeners) {
                    try {
                        listener.onMethodAdd(manage, newAdded);
                    } catch (Exception e) {
                        log.error("doc.err_add_method:interfaceId={}", manage.getId(), e);
                    }

                }

                if (!updatedBeforeMethods.isEmpty()) {
                    List<String> updatedInfos = structedUpdated.stream().map(vs -> {
                        return (String) BeanTool.getProp(vs, keyField);
                    }).collect(Collectors.toList());
                    log.info("docReport.update_methods:manage={},updated={}", manage.getId(),
                            updatedInfos);
                    List<MethodManage> toBeUpdated = methodManageService.fixNoContentMethodsDigest(updatedBeforeMethods);
                    //上报标记为覆盖 删除差量信息
                    if (null != dto.getIsCover() && dto.getIsCover() == 1) {
                        List<Long> methoIds = toBeUpdated.stream().map(MethodManage::getId).collect(Collectors.toList());
                        for (Long methodId : methoIds) {
                            deltaInfoService.removeDelta(methodId);
                        }
                    }
                    List<MethodManage> mergeUpdateMethods = mergeMethodsDeltaInfo(manage, toBeUpdated);

                    Map<Long, List<MethodManage>> id2Methods = updated.stream().collect(Collectors.groupingBy(MethodManage::getId));

                    for (MethodManage mergeUpdateMethod : mergeUpdateMethods) {
                        mergeUpdateMethod.setMergedContentDigest(methodManageService.getContentObjectDigest(mergeUpdateMethod));
                        List<MethodManage> updatedMethods = id2Methods.get(mergeUpdateMethod.getId());
                        if (updatedMethods != null) { // 将updated中的mergedContentDigest更新一下
                            updatedMethods.get(0).setMergedContentDigest(mergeUpdateMethod.getMergedContentDigest());
                        }
                    }


                    for (InterfaceChangeListener listener : listeners) {
                        try {
                            listener.onMethodBeforeUpdate(manage, mergeUpdateMethods);
                        } catch (Exception e) {
                            log.error("doc.err_update_method:interfaceId={}", manage.getId(), e);
                        }
                    }
                    //更新时 更新分数
                    for (InterfaceChangeListener listener : listeners) {
                        try {
                            listener.onMethodUpdate(manage, mergeUpdateMethods);
                        } catch (Exception e) {
                            log.error("onMethodUpdate doc.err_update_method:interfaceId={}", manage.getId(), e);
                        }
                    }


                }
                if (!removed.isEmpty()) {// 删除接口数据会导致不必要的麻烦，这里不再允许删除方法
                    List<Long> removedIds = removed.stream().map(vs -> vs.getId()).collect(Collectors.toList());
                    log.error("docReport.ignore_removed_methods:interfaceId={},methodIds={}", manage.getId(), removedIds);
                    removed = new ArrayList<>();
                }
                /**
                 if(!removedIds.isEmpty()){
                 methodManageService.removeMethodByIds(removedIds);
                 for(MethodManage methodManage : removed){
                 for (InterfaceChangeListener listener : listeners) {
                 try{
                 listener.onMethodRemove(manage,methodManage);
                 }catch (Exception e){
                 log.error("doc.err_remove_method:interfaceId={}",manage.getId(),e);
                 }

                 }
                 }
                 }*/
                for (MethodManage methodManage : updated) {
                    methodManageService.updateById(methodManage);
                }
                // 更新新添加方法的分组信息
                mergeNewMethodGroup(manage, group2MethodManage, path2Methods, keyField);
                // 更新接口的版本信息
                updateInterfaceVersion(manage, existMethods, removed, newAdded, structedUpdated, keyField, dto, groupTreeModel);
                interfaceManageService.updateById(manage);
                return new DocUpdateData(newAdded, authKeyUpdated, removed);
                //return result;
            }

            public List<MethodManage> mergeMethodsDeltaInfo (InterfaceManage  interfaceManage, List < MethodManage > methods){
                List<MethodManage> result = new ArrayList<>();
                for (MethodManage methodManage : methods) {
                    MethodManage clone = new MethodManage();
                    BeanUtils.copyProperties(methodManage, clone);
                    result.add(clone);
                }
                methodManageService.initMethodRefAndDelta(methods, interfaceManage.getId());
                return result;
            }


            /**
             * 报上来新数据后需要把原有版本的数据快照固定下来以及生成新的版本快照
             *
             * @param manage
             * @param existMethods
             * @param newAdded
             * @param updated
             */
            private void updateInterfaceVersion (InterfaceManage
            manage, List < MethodManage > existMethods, List < MethodManage > removed,
                    List < MethodManage > newAdded, List < MethodManage > updated, String keyField, DocReportDto dto,
                    MethodGroupTreeModel treeModel
    ){
                List<String> updatedInfos = updated.stream().map(vs -> {
                    return (String) BeanTool.getProp(vs, keyField);
                }).collect(Collectors.toList());
                List<String> addInfos = newAdded.stream().map(vs -> {
                    return (String) BeanTool.getProp(vs, keyField);
                }).collect(Collectors.toList());
                List<String> removedKeysInfos = removed.stream().map(vs -> {
                    return (String) BeanTool.getProp(vs, keyField);
                }).collect(Collectors.toList());
                log.info("docReport.merge_method:manage={},added={},updated={},removed={}", manage.getId(),
                        addInfos, updatedInfos, removedKeysInfos);
                if (newAdded.isEmpty() && updated.isEmpty() && removed.isEmpty()) {
                    log.info("docReport.doc_has_not_change:appId={}", manage.getAppId());
                    return;
                }
                InterfaceVersion oldVersion = null;
                InterfaceVersion newVersion = new InterfaceVersion();
                newVersion.setInterfaceId(manage.getId());
                if (!StringUtils.isBlank(dto.getCreateDate())) {
                    try {
                        Date date = DateUtil.parseDate(dto.getCreateDate().trim());
                        if (date.getTime() <= System.currentTimeMillis()) { // 大于当前日期说明是无效的
                            newVersion.setCreated(date);
                        }

                    } catch (Exception e) {
                        log.error("doc.err_report_doc_err_parse_date:date={}", dto.getCreateDate().trim(), e);
                    }

                }
                if (manage.getLatestDocVersion() == null) {
                    String version = "1.0.0";
                    manage.setLatestDocVersion(version);
                    newVersion.setVersion(version);
                } else {
                    oldVersion = versionService.getInterfaceVersion(manage.getId(), manage.getLatestDocVersion());
                    String newVersionStr = VersionManager.increaseVersion(manage.getLatestDocVersion(), 2);
                    newVersion.setVersion(newVersionStr);
                    manage.setLatestDocVersion(newVersionStr);
                }

                if (oldVersion != null) { // 旧版本不为空的话需要更新一下快照
                    MethodSnapshot snapshot = new MethodSnapshot();
                    for (MethodManage existMethod : existMethods) {
                        MethodSnapshotItem snapshotItem = new MethodSnapshotItem();
                        snapshotItem.setMethodId(existMethod.getId());
                        snapshotItem.setDigest(existMethod.getMergedContentDigest());
                        snapshotItem.setPath(existMethod.getPath());
                        snapshot.getMethods().add(snapshotItem);
                    }
                    oldVersion.setGroupTreeSnapshot(treeModel);
                    oldVersion.setMethodSnapshot(snapshot);
                    versionService.updateById(oldVersion);
                }
                // 同步新版本
                MethodSnapshot newSnapshot = new MethodSnapshot();
                for (MethodManage newMethod : newAdded) {
                    MethodSnapshotItem snapshotItem = new MethodSnapshotItem();
                    snapshotItem.setMethodId(newMethod.getId());
                    snapshotItem.setDigest(newMethod.getMergedContentDigest());
                    snapshotItem.setPath(newMethod.getPath());
                    newSnapshot.getMethods().add(snapshotItem);
                }
                Map<Long, List<MethodManage>> updatedId2Methods = updated.stream().collect(Collectors.groupingBy(MethodManage::getId));
                for (MethodManage existMethod : existMethods) {
                    MethodSnapshotItem snapshotItem = new MethodSnapshotItem();
                    snapshotItem.setMethodId(existMethod.getId());
                    List<MethodManage> methodManages = updatedId2Methods.get(existMethod.getId());
                    if (methodManages != null) {
                        snapshotItem.setDigest(methodManages.get(0).getDigest());
                    } else {
                        snapshotItem.setDigest(existMethod.getDigest());
                    }
                    if (existMethod.getPath() != null) {
                        snapshotItem.setPath(existMethod.getPath());
                    } else {
                        snapshotItem.setPath(existMethod.getMethodCode());
                    }

                    newSnapshot.getMethods().add(snapshotItem);
                }
                newVersion.setMethodSnapshot(newSnapshot);
                versionService.save(newVersion);
            }
}
