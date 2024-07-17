package com.jd.workflow.console.service.auth;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.cjg.bus.BusInterfaceRpcService;

import com.jd.cjg.bus.request.AliasAddReq;
import com.jd.cjg.bus.request.AliasDeleteReq;
import com.jd.cjg.bus.request.AliasSearchReq;
import com.jd.cjg.bus.vo.AliasInfoVo;
import com.jd.cjg.bus.vo.InterfaceDetailsVo;
import com.jd.cjg.result.Result;
import com.jd.cjg.result.StatusMessage;
import com.jd.workflow.console.base.StatusResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.AuthLevel;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.auth.AppAuthDto;
import com.jd.workflow.console.dto.auth.AuthDto;
import com.jd.workflow.console.dto.auth.JsfInterfaceAuthDto;
import com.jd.workflow.console.dto.auth.UpdateInterfaceAuthDto;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.JsfAlias;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.JsfAliasService;
import com.jd.workflow.console.service.impl.InterfaceManageServiceImpl;
import com.jd.workflow.console.service.impl.MethodManageServiceImpl;
import com.jd.workflow.jsf.enums.JsfRegistrySite;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile("erpLogin")
public class InterfaceAuthService {
    static final Map<String, String> cjgEnv2Env = new HashMap<>();
    static final Map<String, String> env2CjgEnv = new HashMap<>();

    static {

        env2CjgEnv.put("test", "Test");
        env2CjgEnv.put("pre", "Advance");
        env2CjgEnv.put("online", "Online");
        for (Map.Entry<String, String> entry : env2CjgEnv.entrySet()) {
            cjgEnv2Env.put(entry.getValue(), entry.getKey());
        }
    }

    static final String SITE_NAME_ZH = "China";
    @Autowired
    BusInterfaceRpcService rpcService;
    @Autowired
    JsfAliasService jsfAliasService;
    // 接口可以最多关联的app数量
    @Value("${cjg.interfaceMaxRelatedAppCount:20}")
    Integer interfaceMaxRelatedAppCount;
    @Value(("${cjg.jcfUrl:http://cjg-jcf.jd.com}"))
    String cjgJcfUrl;

    @Autowired
    IAppInfoService appInfoService;

    @Autowired
    InterfaceManageServiceImpl interfaceManageService;

    @Autowired
    MethodManageServiceImpl methodManageService;


    @Autowired
    ScheduledThreadPoolExecutor defaultScheduledExecutor;
    @Autowired
    CjgHelper cjgHelper;

    private InterfaceManage validateGetJsfInterface(Long interfaceId) {
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        Guard.notEmpty(interfaceManage, "无效的接口id");
        Guard.assertTrue(InterfaceTypeEnum.JSF.getCode().equals(interfaceManage.getType()), "接口只能为jsf接口");
        return interfaceManage;
    }

    public boolean checkHasAuth(Long interfaceId) {
        InterfaceManage interfaceManage = validateGetJsfInterface(Long.valueOf(interfaceId));
        return StringUtils.isNotBlank(interfaceManage.getCjgAppId()) ? true : false;
    }

    public InterfaceManage queryJsfInterface(String interfaceName, String cjgAppCode) {
        Guard.notEmpty(interfaceName, "接口名称不可为空");
        Guard.notEmpty(cjgAppCode, "appCode不可为空");
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getCjgAppId, cjgAppCode);
        lqw.eq(InterfaceManage::getServiceCode, interfaceName);
        List<InterfaceManage> list = interfaceManageService.list(lqw);
        if (!CollectionUtils.isEmpty(list)) {
            return list.get(0);
        }
        return null;
    }

    private String envMapping(String name) {

        return env2CjgEnv.get(name);
    }

    /**
     * 校验jsf接口是否可以继续关联藏经阁应用，同一个maven坐标只能关联一个应用。
     *
     * @param interfaceId
     * @param cjgAppId
     * @return
     */
    public boolean checkAppCanRelated(Long interfaceId, String cjgAppId) {
        InterfaceManage interfaceManage = validateGetJsfInterface(Long.valueOf(interfaceId));
        List<InterfaceManage> interfaceList = interfaceManageService.getCjgRelatedList(cjgAppId);
        Map<String, List<InterfaceManage>> paths2Interface = interfaceList.stream().collect(Collectors.groupingBy(InterfaceManage::getPath));
        final List<InterfaceManage> existInterfaceList = paths2Interface.get(interfaceManage.getPath());
        if (!CollectionUtils.isEmpty(existInterfaceList)
                && !existInterfaceList.get(0).getCjgAppId().equals(cjgAppId)
        ) {
            throw new BizException("同一个maven坐标下的接口只能关联一个应用，当前接口只能关联" + interfaceList.get(0).getCjgAppName());
        }
        if (paths2Interface.size() > interfaceMaxRelatedAppCount) {
            throw new BizException("当前应用关联的maven坐标超过上限，最多只能关联" + paths2Interface.size() + "个");
        }
        return true;
    }

    public void saveAuthConfig(AuthDto authDto) {
        InterfaceManage interfaceManage = validateGetJsfInterface(Long.valueOf(authDto.getId()));
        Long appId = interfaceManage.getAppId();
        if (appId != null) {
            AppInfo app = appInfoService.getById(appId);
            if (appId != null && !app.getAppCode().equals(authDto.getRelatedAppId())) {
                throw new BizException("鉴权应用必须和所属应用一致");
            }
        }
        if (authDto.isEnableAuth()) {
            Guard.notEmpty(authDto.getRelatedAppId(), "关联应用id不可为空");
            Guard.notEmpty(authDto.getRelatedAppName(), "关联应用名称不可为空");
            com.jd.cjg.bus.request.InterfaceCreateReq req = new com.jd.cjg.bus.request.InterfaceCreateReq();
            req.setAppCode(authDto.getRelatedAppId());
            req.setSite(SITE_NAME_ZH);
            req.setInterfaceCName(interfaceManage.getName());
            req.setInterfaceName(interfaceManage.getServiceCode());
            req.setDevelopmentManager(authDto.getDeveloperMaster());
            req.setProductManagerList(Collections.singletonList(authDto.getProductMaster()));
            req.setCreateBy(UserSessionLocal.getUser().getUserId());
            setJsfAlias(interfaceManage, req);
            setMethods(interfaceManage, req);
            boolean result = cjgHelper.createInterface(req);
            if (!result) {
                throw new BizException("保存鉴权失败");
            }
            /*JcfResult<Boolean> result = rpcService.createInterface(req);
            if(!isSuccess(result)){
                throw new BizException("保存鉴权失败："+result.getMessage());
            }*/
        }
        if (interfaceManage.getAppId() == null) {
            AppInfoDTO relatedApp = appInfoService.findAppByCode(authDto.getRelatedAppId());
            if (relatedApp != null) {
                interfaceManage.setAppId(relatedApp.getId());
            }
        }


        interfaceManage.setIsPublic(authDto.getIsPublic() ? 1 : 0);
        interfaceManage.setCjgAppId(authDto.getRelatedAppId());
        interfaceManage.setCjgAppName(authDto.getRelatedAppName());
        interfaceManageService.updateById(interfaceManage);
    }

    public AuthDto getAuthConfig(Long interfaceId) {
        InterfaceManage interfaceManage = validateGetJsfInterface(interfaceId);
        AuthDto dto = new AuthDto();
        dto.setRelatedAppId(interfaceManage.getCjgAppId());
        dto.setRelatedAppName(interfaceManage.getCjgAppName());
        dto.setIsPublic(interfaceManage.getIsPublic() == 1 ? true : false);
        if (interfaceManage.getCjgAppId() != null) {
            dto.setEnableAuth(true);
            Result<InterfaceDetailsVo> result = rpcService.getInterface(interfaceManage.getCjgAppId(), interfaceManage.getServiceCode());
            if (!isSuccess(result)) {
                throw new BizException("获取鉴权配置失败：" + result.getMessage());
            }
            InterfaceDetailsVo model = result.getModel();

            dto.setId(interfaceId + "");
            if (model != null) {
                dto.setDeveloperMaster(model.getInterfaceDevelopmentManager());
                dto.setProductMaster(CollectionUtils.isEmpty(model.getInterfaceProductManagerList()) ? null : model.getInterfaceProductManagerList().get(0));
            }

        }

        return dto;
    }

    private void setJsfAlias(InterfaceManage interfaceManage, com.jd.cjg.bus.request.InterfaceCreateReq req) {
        List<JsfAlias> jsfAliases = jsfAliasService.aliasAll(interfaceManage.getId());
        List<com.jd.cjg.bus.request.InterfaceCreateReq.Alias> reqAlias = new ArrayList<>();
        for (JsfAlias jsfAlias : jsfAliases) {
            com.jd.cjg.bus.request.InterfaceCreateReq.Alias alias = new com.jd.cjg.bus.request.InterfaceCreateReq.Alias();
            alias.setName(jsfAlias.getAlias());
            alias.setSite(SITE_NAME_ZH);
            alias.setEnvironment(envMapping(jsfAlias.getEnv()));
            reqAlias.add(alias);
        }
        req.setAliasList(reqAlias);
    }

    private void setMethods(InterfaceManage interfaceManage, com.jd.cjg.bus.request.InterfaceCreateReq req) {
        List<MethodManage> interfaceMethods = methodManageService.getInterfaceMethods(interfaceManage.getId());
        interfaceMethods = interfaceMethods.stream().filter(item -> {
            return InterfaceTypeEnum.HTTP.getCode().equals(item.getType()) || InterfaceTypeEnum.JSF.getCode().equals(item.getType());
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(interfaceMethods)) {
            req.setMethodInfoList(interfaceMethods.stream().filter(o -> DataYnEnum.VALID.getCode().equals(o.getYn())).map(o -> {
                com.jd.cjg.bus.request.InterfaceCreateReq.MethodInfo m = new com.jd.cjg.bus.request.InterfaceCreateReq.MethodInfo();
                m.setMethodCName(o.getName());
                m.setMethodName(o.getMethodCode());
                return m;
            }).collect(Collectors.toList()));
        }
    }

    public void syncJsfAlias(Long interfaceId) {
        final InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        Guard.notEmpty(interfaceManage, "无效的接口id");
        if (com.jd.common.util.StringUtils.isEmpty(interfaceManage.getCjgAppId())) {
            return;
        }
        List<JsfAlias> jsfAlias = jsfAliasService.aliasAll(interfaceId);
        AliasSearchReq searchReq = new AliasSearchReq();
        searchReq.setAppCode(interfaceManage.getCjgAppId());
        searchReq.setInterfaceName(interfaceManage.getServiceCode());
        Result<List<AliasInfoVo>> result = rpcService.searchAlias(searchReq);
        if (!isSuccess(result)) {
            log.error("cjg.err_sync_jsf_alias:interfaceId={},msg={}", interfaceId, result);
            return;
        }
        List<JsfAlias> needAdd = new ArrayList<>();
        List<JsfAlias> needUpdate = new ArrayList<>();
        List<JsfAlias> needRemove = new ArrayList<>();
        for (AliasInfoVo aliasInfoVo : result.getModel()) {
            if (!SITE_NAME_ZH.equals(aliasInfoVo.getSite())) {
                continue;
            }
            final JsfAlias alias = toJsfAlias(aliasInfoVo);
            if (!contains(jsfAlias, alias)) {
                needRemove.add(alias);
            }
        }
        final List<JsfAlias> remoteAlias = result.getModel().stream().map(vs -> toJsfAlias(vs)).collect(Collectors.toList());
        for (JsfAlias alias : jsfAlias) {
            if (!contains(remoteAlias, alias)) {
                needAdd.add(alias);
            }
        }

        for (JsfAlias alias : needAdd) {
            AliasAddReq req = new AliasAddReq();
            req.setAppCode(interfaceManage.getCjgAppId());
            req.setInterfaceName(interfaceManage.getServiceCode());
            req.setName(alias.getAlias());
            req.setSite(SITE_NAME_ZH);
            req.setEnvironment(envMapping(alias.getEnv()));
            req.setCreateBy(UserSessionLocal.getUser().getUserId());
            final Result<Boolean> callResult = rpcService.addAlias(req);
            if (!isSuccess(callResult)) {
                throw new BizException("增加jsf别名失败:" + callResult.getMessage());
            }
        }
        for (JsfAlias alias : needRemove) {
            AliasDeleteReq req = new AliasDeleteReq();
            req.setAppCode(interfaceManage.getCjgAppId());
            req.setInterfaceName(interfaceManage.getServiceCode());
            req.setName(alias.getAlias());
            req.setSite(SITE_NAME_ZH);
            req.setEnvironment(envMapping(alias.getEnv()));
            Result callResult = rpcService.deleteAlias(req);

            if (!isSuccess(callResult)) {
                throw new BizException("删除jsf别名失败:" + callResult.getMessage());
            }
        }
    }

    private boolean contains(List<JsfAlias> aliases, JsfAlias alias) {
        for (JsfAlias jsfAlias : aliases) {
            if (jsfAlias.getAlias().equals(alias.getAlias())
                    && jsfAlias.getEnv().equals(alias.getEnv())
                    && jsfAlias.getSite().equals(alias.getSite())
            ) {
                return true;
            }
        }
        return false;
    }

    private JsfAlias toJsfAlias(AliasInfoVo vo) {
        JsfAlias alias = new JsfAlias();
        alias.setAlias(vo.getName());
        alias.setSite(JsfRegistrySite.zh.name());
        alias.setEnv(cjgEnv2Env.get(vo.getEnvironment()));

        return alias;
    }

    private boolean isSuccess(Result result) {
        return StatusMessage.SUCCESS.getCode() == result.getCode();
    }
    private boolean hasAuthInterface(Long appId){
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getAppId, appId);
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        lqw.isNotNull(InterfaceManage::getCjgAppId);
        lqw.eq(InterfaceManage::getType,InterfaceTypeEnum.JSF.getCode());
        lqw.last("  limit 1");
        interfaceManageService.excludeBigTextFiled(lqw);

        return interfaceManageService.list(lqw) != null;
    }
    public List<InterfaceSortModel> listUnAuthInterface(Long appId) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getAppId, appId);
        lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
        //lqw.isNull(InterfaceManage::getCjgAppId);
        lqw.eq(InterfaceManage::getType,InterfaceTypeEnum.JSF.getCode());
        interfaceManageService.excludeBigTextFiled(lqw);
        List<InterfaceManage> interfaceManages = interfaceManageService.list(lqw);
        List<Long> interfaceIds = interfaceManages.stream().map(item -> item.getId()).collect(Collectors.toList());
        if (interfaceIds.isEmpty()) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<MethodManage> methodLqw = new LambdaQueryWrapper<>();
        methodLqw.eq(MethodManage::getYn, 1);
        methodLqw.in(MethodManage::getInterfaceId, interfaceIds);
        methodManageService.excludeBigTextFiled(methodLqw);
        List<MethodManage> methods = methodManageService.list(methodLqw);
        Map<Long, List<MethodManage>> id2Methods = methods.stream().collect(Collectors.groupingBy(MethodManage::getInterfaceId));

        return interfaceManages.stream().map(item -> {
            InterfaceSortModel sortModel = new InterfaceSortModel();
            sortModel.setName(item.getName());
            sortModel.setEnName(item.getServiceCode());
            sortModel.setId(item.getId());
            sortModel.setValid(StringUtils.isBlank(item.getCjgAppId()) ?1:0);// 开启鉴权用的不能再次开启
            List<MethodManage> interfaceMethods = id2Methods.get(item.getId());
            if(interfaceMethods==null){
                interfaceMethods = new ArrayList<>();
            }
            List<MethodSortModel> children = interfaceMethods.stream().map(method -> {
                MethodSortModel methodSortModel = new MethodSortModel();
                methodSortModel.setId(method.getId());
                methodSortModel.setInterfaceType(method.getType());
                methodSortModel.setName(method.getName());
                methodSortModel.setEnName(method.getMethodCode());
                return methodSortModel;
            }).collect(Collectors.toList());
            sortModel.getChildren().addAll(children);
            return sortModel;
        }).collect(Collectors.toList());
    }
    @Transactional
    public boolean addJsfInterface( AppAuthDto appAuthDto,String cookie){
        Guard.notEmpty(appAuthDto.getAppId(),"appId不可为空");
        List<InterfaceManage> interfaceManages = interfaceManageService.listInterfaceByIds(appAuthDto.getInterfaceIds());
        Guard.notEmpty(interfaceManages,"无效的接口id");
        for (InterfaceManage interfaceManage : interfaceManages) {
            if(StringUtils.isNotBlank(interfaceManage.getCjgAppId())){
                throw new BizException(interfaceManage.getName()+"已开启鉴权，请勿重复开启");
            }
        }


        AppInfo app = appInfoService.getById(appAuthDto.getAppId());
        Guard.notEmpty(app,"无效的appId");
        boolean hasAuthInterface = hasAuthInterface(appAuthDto.getAppId());
        if(!hasAuthInterface){
            saveDuccAuth(app,cookie);
        }
        Guard.notEmpty(appAuthDto.getInterfaceIds(),"接口id不可为空");
        if(StringUtils.isNotBlank(app.getAuthLevel()) &&
                StringUtils.isNotBlank(appAuthDto.getAuthLevel())
            && !app.getAuthLevel().equals( appAuthDto.getAuthLevel())
        ){
            throw  new BizException("鉴权级别已设置，不可修改");
        }
        String cjgAppCode = app.getCjgAppId();
        if(StringUtils.isBlank(cjgAppCode)){
            cjgAppCode = app.getAppCode();
        }

        AppInfoDTO existedApp = cjgHelper.getCjgComponetInfoByCode(cjgAppCode);
        if(existedApp == null){
            app.setAuthLevel(appAuthDto.getAuthLevel());
            String cjgId = cjgHelper.createCjgComponent(AppInfoDTO.from(app));
            app.setCjgAppId(cjgId);
            appInfoService.updateById(app);
        }else{
            if(!ObjectHelper.equals(existedApp.getAuthLevel(), appAuthDto.getAuthLevel())){
                throw  new BizException("藏经阁应用鉴权级别已设置为"+ AuthLevel.getByCode(existedApp.getAuthLevel()).getDescription() +"，不可更改·");
            }
            app.setAuthLevel(appAuthDto.getAuthLevel());
            app.setCjgAppId(existedApp.getCjgAppId());
            appInfoService.updateById(app);
        }

        createAuthReq(app.getAppCode(),appAuthDto,interfaceManages);
        return true;
    }

    public void updateJsfReq(UpdateInterfaceAuthDto dto){
        Guard.notEmpty(dto.getAppId(),"appId无效");
        Guard.notEmpty(dto.getInterfaceId(),"interfaceId无效");
        AppInfoDTO app = appInfoService.findApp(dto.getAppId());
        InterfaceManage interfaceManage = interfaceManageService.getById(dto.getInterfaceId());
        com.jd.cjg.bus.request.InterfaceCreateReq req = new com.jd.cjg.bus.request.InterfaceCreateReq();
        req.setAppCode(app.getAppCode());
        req.setSite(SITE_NAME_ZH);
        req.setInterfaceCName(interfaceManage.getName());
        req.setInterfaceName(interfaceManage.getServiceCode());
        req.setDevelopmentManager(dto.getDevelopmentManager());
        req.setDeveloperList(dto.getDeveloperList());
        req.setProductManagerList(dto.getProductManagerList());
        req.setCreateBy(UserSessionLocal.getUser().getUserId());
        boolean result = cjgHelper.createInterface(req);
        if (!result) {
            throw new BizException("修改鉴权失败");
        }
    }
    private boolean createAuthReq(String  appCode,AppAuthDto dto,List<InterfaceManage> interfaceManages){
        for (InterfaceManage interfaceManage : interfaceManages) {
            com.jd.cjg.bus.request.InterfaceCreateReq req = new com.jd.cjg.bus.request.InterfaceCreateReq();
            req.setAppCode(appCode);
            req.setSite(SITE_NAME_ZH);
            req.setInterfaceCName(interfaceManage.getName());
            req.setInterfaceName(interfaceManage.getServiceCode());
            req.setDevelopmentManager(dto.getDevelopmentManager());
            req.setDeveloperList(dto.getDeveloperList());
            req.setProductManagerList(dto.getProductManagerList());
            req.setCreateBy(UserSessionLocal.getUser().getUserId());
            setJsfAlias(interfaceManage, req);
            setMethods(interfaceManage, req);
            boolean result = cjgHelper.createInterface(req);
            if (!result) {
                throw new BizException("保存鉴权失败");
            }
            interfaceManage.setCjgAppId(appCode);
            LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();
            luw.set(InterfaceManage::getCjgAppId,appCode);
            luw.eq(InterfaceManage::getId,interfaceManage.getId());
            interfaceManageService.update(luw);
        }
        return true;

    }
    public JsfInterfaceAuthDto getJsfAuthDetail(Long interfaceId){
        InterfaceManage record = interfaceManageService.getById(interfaceId);
        AppInfo app = appInfoService.getById(record.getAppId());
        JsfInterfaceAuthDto dto = new JsfInterfaceAuthDto();
        dto.setId(record.getId());
        dto.setName(record.getName());
        dto.setServiceCode(record.getServiceCode());
        dto.setUpdated(StringHelper.formatDate(record.getModified(),"yyyy-MM-dd HH:mm:ss"));
        Result<InterfaceDetailsVo> result = rpcService.getInterface(app.getAppCode(), record.getServiceCode());
        if(result.getModel() != null){
            dto.setDevelopmentManager(result.getModel().getInterfaceDevelopmentManager());
            dto.setProductManagerList(result.getModel().getInterfaceProductManagerList());
            dto.setDeveloperList(result.getModel().getDeveloperList());
        }
        return dto;
    }
    public Page<JsfInterfaceAuthDto> jsfInterfaceAuthDtos(Long appId,Long current,Long size){
        if(size == null){
            size = 10L;
        }
        if(size > 100){
            throw new BizException("size不能大于100");
        }
        if(current == null){
            current = 1L;
        }
        AppInfo app = appInfoService.getById(appId);
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getAppId,appId);
        lqw.eq(InterfaceManage::getYn,1);
        lqw.eq(InterfaceManage::getType,InterfaceTypeEnum.JSF.getCode());
        lqw.isNotNull(InterfaceManage::getCjgAppId);

        interfaceManageService.excludeBigTextFiled(lqw);
        Page<InterfaceManage> interfacePage = interfaceManageService.page(new Page<>(current,size),lqw);

        List<Future> futures = new ArrayList<>();
        List<JsfInterfaceAuthDto> dtos = new ArrayList<>();
        for (InterfaceManage record : interfacePage.getRecords()) {
            JsfInterfaceAuthDto dto = new JsfInterfaceAuthDto();
            dto.setId(record.getId());
            dto.setName(record.getName());
            dto.setServiceCode(record.getServiceCode());
            dto.setUpdated(StringHelper.formatDate(record.getModified(),"yyyy-MM-dd HH:mm:ss"));
            Future future = defaultScheduledExecutor.submit(() -> {
                Result<InterfaceDetailsVo> result = rpcService.getInterface(app.getAppCode(), record.getServiceCode());
                if(result.getModel() != null){
                    dto.setDevelopmentManager(result.getModel().getInterfaceDevelopmentManager());
                    dto.setProductManagerList(result.getModel().getInterfaceProductManagerList());
                    dto.setDeveloperList(result.getModel().getDeveloperList());
                }
                dtos.add(dto);
                return null;
            });
            futures.add(future);
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (Exception e) {
                throw new BizException("获取失败",e);
            }
        }
        Page<JsfInterfaceAuthDto> page = new Page<>(interfacePage.getCurrent(), interfacePage.getSize(), interfacePage.getTotal());
        page.setRecords(dtos);
        return page;
    }
    public void saveDuccAuth(AppInfo app,String cookieValue){
        Map<String,Object> headers = new HashMap<>();
        headers.put("Cookie",cookieValue);
        String cjgAppKey = null;
        final AppInfoDTO info = cjgHelper.getCjgComponetInfoByCode(app.getAppCode());
        if (info != null) {
            cjgAppKey = info.getCjgAppKey();
        }
        if(StringUtils.isEmpty(cjgAppKey)){
            throw new BizException("开启鉴权失败:获取藏经阁配置失败,请联系管理员");
        }

            BasicCookieStore cookieStore = new BasicCookieStore();


           /* BasicClientCookie cookie = new BasicClientCookie("sso.jd.com", cookieValue);
            cookie.setDomain(".jd.com");
            cookie.setPath("/");
            cookieStore.addCookie(cookie);*/

            RequestClient requestClient = new RequestClient(cookieStore);

        StatusResult<Map<String, Object>> status = requestClient.post(cjgJcfUrl+"/jcfapi/appauth/init/" + cjgAppKey + "/China", (Map<String,Object>)null, headers,null,
                new TypeReference<StatusResult<Map<String, Object>>>() {
                });

        if(!status.getStatus().equals(200)){
            throw new BizException("开启鉴权失败："+status.getMessage());
        }
        boolean success = (boolean) status.getData().get("success");
        if(!success){
            throw new BizException("开启鉴权失败："+status.getMessage());
        }
    }

}
