package com.jd.workflow.console.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.cjg.bus.BusInterfaceRpcService;
import com.jd.cjg.bus.request.ComponentUpdateReq;
import com.jd.cjg.bus.vo.ComponentInfoVo;
import com.jd.cjg.graph.GraphProductPanoramaProvider;
import com.jd.cjg.result.Result;
import com.jd.cjg.result.StatusMessage;
import com.jd.jsf.open.api.ProviderService;
import com.jd.jsf.open.api.domain.Server;
import com.jd.jsf.open.api.vo.request.QueryProviderRequest;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.DateUtil;
import com.jd.workflow.console.base.MD5Util;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.AppUserTypeEnum;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.dao.mapper.AppInfoMapper;
import com.jd.workflow.console.dto.*;
import com.jd.workflow.console.dto.app.AppMembers;
import com.jd.workflow.console.dto.jsf.JSFArgBuilder;
import com.jd.workflow.console.dto.manage.AppAndSecret;
import com.jd.workflow.console.dto.manage.AppSearchResult;
import com.jd.workflow.console.dto.role.UserRoleDTO;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.AppInfoMembers;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.doc.importer.CjgApiImporter;
import com.jd.workflow.console.service.env.IEnvConfigService;
import com.jd.workflow.console.service.listener.InterfaceChangeListener;
import com.jd.workflow.console.service.remote.api.JDosAppOpenService;
import com.jd.workflow.console.service.remote.api.dto.jdos.JDosAppInfo;
import com.jd.workflow.console.service.remote.api.dto.jdos.JagileJdosSiteEnum;
import com.jd.workflow.console.service.remote.api.dto.jdos.JdosAppMembers;
import com.jd.workflow.console.utils.SafeUtil;
import com.jd.workflow.console.service.role.AccRoleServiceAdapter;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 项目名称：parent
 * 类 名 称：AppInfoServiceImpl
 * 类 描 述：应用service
 * 创建时间：2022-11-16 17:20
 * 创 建 人：wangxiaofei8
 */
@Service
@Slf4j
public class AppInfoServiceImpl extends ServiceImpl<AppInfoMapper, AppInfo> implements IAppInfoService {
    static final String NO_APP_NAME = "J-API项目入口";
    static final String CJG_API_URL = "http://cjg-api.jd.com";
    @Autowired
    private CjgHelper cjgHelper;

    @Autowired
    private UserHelper userHelper;
    @Autowired(required = false)
    BusInterfaceRpcService cjgBusInterfaceRpcService;
    @Autowired
    ConfigInfoService configInfoService;
    @Autowired
    MemberRelationServiceImpl memberRelationService;
    @Autowired
    UserInfoServiceImpl userInfoService;

    @Autowired
    List<InterfaceChangeListener> listeners;
    @Autowired
    CjgApiImporter cjgApiImporter;

    @Autowired
    GraphProductPanoramaProvider graphProductPanoramaProvider;

    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    private IAppInfoMembersService appInfoMembersService;

    @Autowired
    JDosAppOpenService jDosAppOpenService;

    @Resource
    ProviderService providerService;

    @Autowired
    private AccRoleServiceAdapter accRoleServiceAdapter;

    @Autowired
    private IMethodManageService methodManageService;

    @Autowired
    private IEnvConfigService envConfigService;
    @Value("${app.sync_create_cjg_app:false}")
    private boolean syncCreateCjgApp = false;

    @Autowired
    private AppInfoMapper  appInfoMapper;

    @Override
    public Long addApp(AppInfoDTO dto) {
        /*if(StringUtils.isNotBlank(dto.getAuthLevel())){
            throw new BizException("鉴权级别不需要设置！");
        }*/
        //校验新增基本参数
        dto.checkAddInfo();
        dto.setId(null);
        if (StringUtils.isNotBlank(dto.getCjgAppId()) && !dto.getAppCode().equals(dto.getCjgAppId())) {
            throw new BizException("导入的cjgAppId与appCode必须保持一致！");
        }
        //  Guard.notEmpty(dto.getJdosAppSysInfo(),"jdos应用信息不能为空");
        String cjgId = null;
        //校验code是否重复
        AppInfoDTO existedApp = cjgHelper.getCjgComponetInfoByCode(dto.getAppCode());
        if (existedApp != null) {
            cjgId = dto.getAppCode();
           /* if (StringUtils.isBlank(dto.getCjgAppId())) {
                throw new BizException("已经存在此应用，请联系管理员！");
            }*/
        } else {
            if(syncCreateCjgApp){ // 测试环境没有更新，需要同步创建藏经阁app
                cjgId = cjgHelper.createCjgComponent(dto);
            }
            //
        }
        //校验联调平台应用code重复
        AppInfo lastObj = this.getOne(Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getAppCode, dto.getAppCode()).eq(AppInfo::getYn, DataYnEnum.VALID.getCode()));
        if (lastObj != null) {
            BizException e = new BizException("应用code已经存在!");
            e.data(lastObj.getId());
            throw e;
        }
        AppInfo entity = new AppInfo();
        BeanUtils.copyProperties(dto, entity);
        entity.setMembers(dto.buildMembers());
        entity.setCjgAppId(cjgId);
        entity.setJdosAppCode(dto.getAppCode().substring("J-dos-".length()));
        entity.setSite(dto.getSite());
        entity.setAppSecret(MD5Util.MD5Encode(dto.getAppCode() + DateUtil.getCurrentDateMillTime()));
        Date opTime = new Date();
        if (dto.getCreated() != null) {
            entity.setCreated(dto.getCreated());
        } else {
            entity.setCreated(opTime);
        }
        if (dto.getModified() != null) {
            entity.setModified(dto.getModified());
        } else {
            entity.setModified(opTime);
        }

        entity.setCreator(UserSessionLocal.getUser().getUserId());
        entity.setModifier(UserSessionLocal.getUser().getUserId());
        if (StringUtils.isBlank(dto.getTenantId())) {
            entity.setTenantId(UserSessionLocal.getUser().getTenantId());
        }
        if(StringUtils.isBlank(entity.getTenantId()))        {
            entity.setTenantId(configInfoService.getDefaultTenantId());
        }
        entity.setYn(DataYnEnum.VALID.getCode());
        boolean save = save(entity);
        appInfoMembersService.saveMembersByStr(entity, entity.getAppCode());
        for (InterfaceChangeListener listener : listeners) {
            listener.onAppAdd(entity);
        }
        if (!save) {
            log.error("IAppInfoService.addApp execute save but return false , param={}>>>>>>>", JSON.toJSONString(entity));
        }
        if (existedApp != null) {
            dto.setModifier(entity.getModifier());
            boolean syncRes = syncCjgInfo(dto, existedApp);
            if (!syncRes) {
                throw new BizException("新增导入应用时同步更新藏经阁应用异常！");
            }
        }
        return entity.getId();
    }


    @Override
    public AppInfo findByJdosAppCode(String jdosAppCode) {
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AppInfo::getAppCode, "J-dos-"+jdosAppCode);
        lqw.eq(AppInfo::getYn,1);
        List<AppInfo> list = list(lqw);
        if(list.isEmpty()){
            List<AppInfo> appInfos = initJdosApp(jdosAppCode, null);
            return appInfos.get(0);
        }
        return list.get(0);
    }

    @Override
    public void syncDeptInfo(AppInfo appInfo) {
        //更新部门信息
        JDosAppInfo info = null;
        try {
            info = jDosAppOpenService.queryJdosAppInfo(appInfo.getJdosAppCode(), appInfo.getSite());
        } catch (Exception e) {
            log.error("获取部门数据失败,appId--{}", appInfo.getId(), e);
        }
        if (null != info && StringUtils.isNotBlank(info.getAppDepartment())) {
            log.info("更新部门之前数据{}", JSONObject.toJSONString(appInfo));
            appInfo.setDept(info.getAppDepartment());
            LambdaUpdateWrapper<AppInfo> updateWrapper = new LambdaUpdateWrapper<AppInfo>();
            updateWrapper.set(AppInfo::getDept, info.getAppDepartment());
            updateWrapper.eq(AppInfo::getId, appInfo.getId());
            updateWrapper.eq(AppInfo::getJdosAppCode, appInfo.getJdosAppCode());
            updateWrapper.eq(AppInfo::getYn, 1);
            log.info("更新部门sql{},{}", updateWrapper.getSqlSet(), updateWrapper.getTargetSql());
            update(updateWrapper);
    }
    }


    @Override
    public void syncMembersFromJdos(AppInfo appInfo) {
//       更新部门信息
//        syncDeptInfo(appInfo);

        JdosAppMembers appMembers = jDosAppOpenService.queryJdosAppMembersAppCode(appInfo.getJdosAppCode(), appInfo.getAppCode());
        if (appMembers != null) {
            Set<String> jdosMembers = new HashSet<>();

            addIfNotEmpty(jdosMembers, appMembers.getSystemAdmin());
            addIfNotEmpty(jdosMembers, appMembers.getAppAdmin());
            addIfNotEmpty(jdosMembers, appMembers.getAppTester());
            addIfNotEmpty(jdosMembers, appMembers.getSystemOp());
            addIfNotEmpty(jdosMembers, appMembers.getAppOwner());
            addIfNotEmpty(jdosMembers, appMembers.getAppOp());
            addIfNotEmpty(jdosMembers, appMembers.getSystemOwner());
            addIfNotEmpty(jdosMembers, appMembers.getSystemTester());
            JdosAppInfoDto jDosAppInfo = new JdosAppInfoDto();


            jDosAppInfo.splitMembers(appInfo.getMembers());

            jDosAppInfo.setJdosMembers(new ArrayList<>(jdosMembers));
            if (!ObjectHelper.isEmpty(appMembers.getAppOwner())) {
                jDosAppInfo.setJdosOwner(appMembers.getAppOwner().get(0));
            }


            syncJdosAppMembersToCjg(appInfo.getAppCode(), jDosAppInfo);
            appInfo.setMembers(jDosAppInfo.buildMembers());
            updateById(appInfo);
            appInfoMembersService.saveMembersByStr(appInfo, appInfo.getAppCode());
        }
    }

    private void addIfNotEmpty(Collection<String> members, List<String> target) {
        if (target != null) {
            members.addAll(target);
        }
    }

    // 应用成员从jdos同步到藏经阁，只能更新藏经阁的其他成员。其他成员=从jdos应用同步过来的+藏经阁原来的成员
    private boolean syncJdosAppMembersToCjg(String cjgAppCode, JdosAppInfoDto dto) {
        Result<ComponentInfoVo> result = cjgBusInterfaceRpcService.getComponentInfoByCode(cjgAppCode);
        log.info("app.get_cjg_app_info:cjgAppCode={},result={}", cjgAppCode, JsonUtils.toJSONString(result));
        ComponentInfoVo vo = result.getModel();

        if (result == null || vo == null) return false;

        Set<String> members = new HashSet<>();
        List<String> allMembers = new ArrayList<>();
        {

            if (StringUtils.isNotBlank(vo.getProjectManager())) {
                allMembers.add(vo.getProjectManager());
            }
            addIfNotEmpty(allMembers, vo.getDeveloperList());
            addIfNotEmpty(allMembers, vo.getProductManagerList());
            addIfNotEmpty(allMembers, vo.getTesterList());
            addIfNotEmpty(allMembers, vo.getTesterRelList());
            if (dto.getJdosMembers() != null) {
                for (String jdosMember : dto.getJdosMembers()) {
                    if (!allMembers.contains(jdosMember)) {
                        members.add(jdosMember);
                    }
                }
            }
            if (StringUtils.isNotBlank(dto.getJdosOwner())) { // 设置jdos成员
                members.add(dto.getJdosOwner());
            }
            if (vo.getDeveloperList() != null) {
                members.addAll(vo.getDeveloperList());
            }
            if (dto.getMembers() != null) {
                for (String member : dto.getMembers()) {
                    if (!allMembers.contains(member)) {
                        members.add(member);
                    }
                }
            }
        }

        ComponentUpdateReq req = new ComponentUpdateReq();
        req.setAppCode(vo.getAppCode());
        req.setAppName(vo.getAppName());
        req.setProjectManager(vo.getProjectManager());
        req.setDeveloperList(new ArrayList<>(members));
        req.setTesterRelList(vo.getTesterRelList());
        req.setProductManagerList(vo.getProductManagerList());
        req.setTesterList(vo.getTesterList());
        req.setDesc(dto.getDesc());
        // req.setDevLanguage(dto.getDevLanguage());
        /*if(dto.getAppTypeReqList()!=null){
            req.setAppTypeReqList(dto.getAppTypeReqList().stream().map(item->item.toReq()).collect(Collectors.toList()));
        }*/

        req.setModifyBy(UserSessionLocal.getUser() == null ? null : UserSessionLocal.getUser().getUserId());
        Result<Boolean> response = null;
        try {
            log.info("busInterfaceRpcService.updateComponentByCode request={} ", JSON.toJSONString(req));
            response = cjgBusInterfaceRpcService.updateComponentByCode(req);
            log.info("busInterfaceRpcService.updateComponentByCode response={} ", JSON.toJSONString(response));
            if (response == null || response.getCode() != StatusMessage.SUCCESS.getCode()) {
                log.error("busInterfaceRpcService.updateComponentByCode call return data exception>>>>>> request={},response={} ", req, JSON.toJSONString(response));
                return false;
            }
            return BooleanUtils.isTrue(response.getModel());
        } catch (Exception e) {
            log.error("busInterfaceRpcService.updateComponentByCode call occur exception>>>>>> ", e);
            return false;
        }
    }

    @Override
    public Long syncCjgAppToLocal(String cjgAppCode, boolean delete) {
        Result<ComponentInfoVo> result = cjgBusInterfaceRpcService.getComponentInfoByCode(cjgAppCode);
        log.info("app.get_cjg_app_info:cjgAppCode={},result={}", cjgAppCode, JsonUtils.toJSONString(result));
        ComponentInfoVo model = result.getModel();
        if (result == null || model == null) return null;
        AppInfoDTO appInfoDTO = new AppInfoDTO();// 这里成员主要是同步藏经阁成员
        appInfoDTO.setAppCode(model.getAppCode());
        appInfoDTO.setAppName(model.getAppName());
        appInfoDTO.setAuthLevel(model.getAuthLevel());
        appInfoDTO.setTenantId(configInfoService.getDefaultTenantId());
        appInfoDTO.setCjgAppId(model.getAppCode());
        //  appInfoDTO.setAppName(model.getAppName());
        appInfoDTO.setOwner(Collections.singletonList(model.getProjectManager()));
        appInfoDTO.setJdosOwner(model.getProjectManager());
        appInfoDTO.setMember(model.getDeveloperList());
        appInfoDTO.setTestMember(model.getTesterRelList());
        appInfoDTO.setDesc(model.getDesc());

        appInfoDTO.setProductor(model.getProductManagerList());
        appInfoDTO.setTester(model.getTesterList());
        final AppInfo app = findApp(model.getAppCode());
        if (app != null) {
            //更新该应用下接口的部门信息。
            updateInterfaceInfoByAppInfo(app);
            app.setAppCode(model.getAppCode());
            app.setAppName(model.getAppName());
            app.setDesc(model.getDesc());
            if (app.getTenantId() == null) {
                app.setTenantId(configInfoService.getDefaultTenantId());
            }
            //app.setMembers(appInfoDTO.buildMembers()); 成员不再更新
            if (delete) { //暂时不可以删除接接口吧
                app.setYn(0);
            }
            updateById(app);
            // appInfoMembersService.saveMembersByStr(app,app.getAppCode());
            return app.getId();
        }
        if (UserSessionLocal.getUser() == null) {
            String member = model.getProjectManager();
            UserInfoInSession userInfoInSession = new UserInfoInSession();
            userInfoInSession.setUserId(member);
            userInfoInSession.setUserName(member);
            userInfoInSession.setTenantId(configInfoService.getFixTenantId());
            UserSessionLocal.setUser(userInfoInSession);
        }
        return addAppInternal(appInfoDTO, model.getAppCode());
    }

    @Override
    public void syncJdosMembers() {
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AppInfo::getYn, 1);
        lqw.isNotNull(AppInfo::getJdosAppCode);
        List<AppInfo> appInfos = list(lqw);
        for (AppInfo appInfo : appInfos) {
            syncMembersFromJdos(appInfo);
        }
    }

    @Override
    public List<Long> modifyInfoYn(AppInfo info,List<Map<String,String>> ids,Integer yn) {
//       if(yn==3){
//           LambdaUpdateWrapper<AppInfo> lqw = new LambdaUpdateWrapper<>();
////           lqw.inSql(AppInfo::getId,"select id from (select max(id) as id,count(1) from app_info where yn=0 and app_code not in (select distinct app_code from app_info where yn=1) group by app_code having count(1)>1) tt");
//           List<Long> st=list(lqw).stream().map(AppInfo::getId).collect(Collectors.toList());
//          return st;
//       }
        if(CollectionUtils.isNotEmpty(ids)){
            for (Map<String, String> inf : ids) {
                LambdaUpdateWrapper<AppInfo> lqw = new LambdaUpdateWrapper<>();
                lqw.set(AppInfo::getYn,Integer.valueOf(inf.get("yn")));
                lqw.set(StringUtils.isNotBlank(inf.get("code")),AppInfo::getAppCode,inf.get("code"));
                lqw.set(StringUtils.isNotBlank(inf.get("secret")),AppInfo::getAppSecret,inf.get("secret"));
                lqw.set(StringUtils.isNotBlank(inf.get("name")),AppInfo::getAppName,inf.get("name"));
                lqw.set(StringUtils.isNotBlank(inf.get("mem")),AppInfo::getMembers,inf.get("mem"));
                lqw.set(StringUtils.isNotBlank(inf.get("cjgAppId")),AppInfo::getCjgAppId,inf.get("cjgAppId"));
                lqw.set(StringUtils.isNotBlank(inf.get("desc")),AppInfo::getDesc,inf.get("desc"));
                lqw.eq(AppInfo::getId,inf.get("id"));
                update(lqw);
            }
        }else{
            LambdaUpdateWrapper<AppInfo> lqw = new LambdaUpdateWrapper<>();
            lqw.set(AppInfo::getYn,yn==null?0:yn);
            lqw.set(StringUtils.isNotBlank(info.getAppCode()),AppInfo::getAppCode,info.getAppCode());
            lqw.set(StringUtils.isNotBlank(info.getAppSecret()),AppInfo::getAppSecret,info.getAppSecret());
            lqw.set(StringUtils.isNotBlank(info.getAppName()),AppInfo::getAppName,info.getAppName());
            lqw.set(StringUtils.isNotBlank(info.getMembers()),AppInfo::getMembers,info.getMembers());
            lqw.set(StringUtils.isNotBlank(info.getCjgAppId()),AppInfo::getCjgAppId,info.getCjgAppId());
            lqw.eq(AppInfo::getId,info.getId());
            update(lqw);
        }

        return null;
    }

    @Override
    public void removeById(List<Long> ids) {
        LambdaQueryWrapper<AppInfo> wrap=new LambdaQueryWrapper<>();
        wrap.in(AppInfo::getId,ids);
        wrap.eq(AppInfo::getYn,0);
        remove(wrap);
    }


    @Override
    public void syncDeptt(AppInfo info) {
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(null!=info&&StringUtils.isNotBlank(info.getJdosAppCode()),AppInfo::getJdosAppCode,info.getJdosAppCode());
        lqw.eq(null!=info&&info.getId()!=null,AppInfo::getId, info.getId());
        lqw.eq(AppInfo::getYn, 1);
        lqw.isNotNull(AppInfo::getJdosAppCode);
        List<AppInfo> appInfos = list(lqw);
        for (AppInfo appInfo : appInfos) {
            syncDeptInfo(appInfo);
        }
    }

    @Override
    public void syncJdosMembers(Long appId) {
        AppInfo appInfo = getById(appId);
        if (appInfo == null) {
            return;
        }
        syncMembersFromJdos(appInfo);
    }


    private Long addAppInternal(AppInfoDTO dto, String cjgId) {
        AppInfo entity = new AppInfo();
        BeanUtils.copyProperties(dto, entity);
        entity.setMembers(dto.buildMembers());
        if (cjgId != null) {
            entity.setCjgAppId(cjgId);
            String appCode = cjgId.substring("J-dos-".length());
            entity.setJdosAppCode(appCode);
        }

        entity.setAppSecret(MD5Util.MD5Encode(dto.getAppCode() + DateUtil.getCurrentDateMillTime()));
        Date opTime = new Date();
        entity.setCreated(opTime);
        entity.setModified(opTime);
        entity.setCreator(UserSessionLocal.getUser().getUserId());
        entity.setModifier(UserSessionLocal.getUser().getUserId());
        String tenantId = dto.getTenantId();
        if (StringHelper.isEmpty(tenantId)) {
            tenantId = UserSessionLocal.getUser().getTenantId();
        }
        entity.setTenantId(tenantId);
        entity.setYn(DataYnEnum.VALID.getCode());
        boolean save = save(entity);
        appInfoMembersService.saveMembersByStr(entity, entity.getAppCode());
        if(entity.getJdosAppCode() != null){
            syncMembersFromJdos(entity);
        }
        if (!save) {
            log.error("IAppInfoService.addApp execute save but return false , param={}>>>>>>>", JSON.toJSONString(entity));
        }
        return entity.getId();
    }

    @Override
    public Boolean modifyApp(AppInfoDTO dto) {
        return modifyApp(dto, false);
    }

    @Override
    public Boolean modifyApp(AppInfoDTO dto, boolean skipValidate) {

        //校验修改参数
        dto.checkModifyAppInfo();
        dto.setCjgAppId(null);
        dto.setAppSecret(null);
        dto.setAppCode(null);
        AppInfo app = getAppInfoById(dto.getId());
        if (!skipValidate) {
            checkAuth(app);
        }

        AppInfo entity = new AppInfo();
        entity.setId(dto.getId());
        entity.setAppName(dto.getAppName());
        entity.setAppCode(app.getAppCode());
        entity.setDesc(dto.getDesc());
        entity.setAuthLevel(dto.getAuthLevel());
        //BeanUtils.copyProperties(dto,entity);
        entity.setMembers(dto.buildMembers());
        entity.setAppType(dto.getAppType());
        Date opTime = new Date();
        if (dto.getModified() != null) {
            entity.setModified(dto.getModified());
        } else {
            entity.setModified(opTime);
        }

        entity.setModifier(UserSessionLocal.getUser().getUserId());
        //更新藏经阁应用
        dto.setAppCode(app.getAppCode());
        dto.setCjgAppId(app.getCjgAppId());
        dto.setModifier(entity.getModifier());
        if(StringUtils.isNotBlank(entity.getCjgAppId())){
            //更新藏经阁
            AppInfoDTO existedApp = new AppInfoDTO();
            existedApp.splitMembers(app.getMembers());
            existedApp.setAppName(app.getAppName());
            boolean updateCjgAppRes = syncCjgInfo(dto, existedApp);
            if (!updateCjgAppRes) {
                throw new BizException("同步更新藏经阁应用异常！");
            }
        }

        appInfoMembersService.saveMembersByStr(entity, app.getAppCode());

        boolean updated = updateById(entity);
        for (InterfaceChangeListener listener : listeners) {
            listener.onAppUpdate(app, entity);
        }
        return updated;
    }

    @Override
    public Boolean removeApp(Long id, String cookieValue) {
        final AppInfo app = getAppInfoById(id);
        checkAuth(app);
        AppInfo entity = new AppInfo();
        entity.setId(id);
        entity.setYn(DataYnEnum.INVALID.getCode());
        entity.setModified(new Date());
        entity.setModifier(UserSessionLocal.getUser().getUserId());
        final AppInfoDTO dto = cjgHelper.getCjgComponetInfoByCode(app.getAppCode());
        if (StringUtils.isNotBlank(dto.getCjgAppKey())) {
          /*  try{
                removeCjgApp(cookieValue,dto.getCjgAppKey());
            }catch (Exception e){
                log.error("app.err_reomove_app:appId={}",dto.getCjgAppKey(),e);
            }*/

        }
        for (InterfaceChangeListener listener : listeners) {
            listener.onAppRemove(entity);
        }

        return updateById(entity);
    }

    @Override
    public AppMembers getJdosAppMembers(String appCode) {
        JdosAppMembers jdosAppMembers = jDosAppOpenService.queryJdosAppMembersAppCode(appCode, null);

        return AppMembers.from(jdosAppMembers);
    }

    private Map<String, Object> removeCjgApp(String cookieValue, String id) {
        RequestClient requestClient = new RequestClient();
        Map<String, Object> params = new HashMap<>();
        params.put("componentId", id);
        Map<String, Object> headers = new HashMap<>();
        headers.put("Cookie", "sso.jd.com=" + cookieValue);
        final Map<String, Object> result = requestClient.get(CJG_API_URL + "/api/component/deleteComponent", params, headers, new TypeReference<Map<String, Object>>() {
        });
        log.info("cjg.remove_app:id={},result={}", id, JSON.toJSONString(result));
        Map<String, Object> data = (Map<String, Object>) result.get("data");
        if (data != null && !ObjectHelper.equals(data.get("success"), true)) {
            log.info("cjg.err_remove_app:id={},result={}", id, JSON.toJSONString(result));
        }
        return result;
    }
    public void initJdosAppAndMembers(){
        List<AppInfo> appInfos = null;
        {
            LambdaQueryWrapper<AppInfo> lqw = Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getYn, DataYnEnum.VALID.getCode());

            lqw.isNull(AppInfo::getJdosAppCode);
            lqw.and(wrapper -> {
                wrapper.inSql(AppInfo::getId, "select app_id from interface_manage where yn=1 and related_id is null ")
                        .or().likeRight(AppInfo::getAppCode, "J-dos-");
            });
            appInfos = list(lqw);
        }

        for (AppInfo appInfo : appInfos) {
            appInfo.setSite("cn");
            if (appInfo.getAppCode().startsWith("J-dos-")) {
                String appCode = appInfo.getAppCode().substring("J-dos-".length());
                log.info("jdos.update_app_code:appCode={}", appCode);
                appInfo.setJdosAppCode(appCode);
                updateById(appInfo);
                syncJdosMembers(appInfo.getId());
            }
        }
    }

    @Override
    public void initJdosApp() {
        List<AppInfo> appInfos = null;
        {
            LambdaQueryWrapper<AppInfo> lqw = Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getYn, DataYnEnum.VALID.getCode());

            lqw.isNull(AppInfo::getJdosAppCode);


            lqw.and(wrapper -> {
                wrapper.inSql(AppInfo::getId, "select app_id from interface_manage where yn=1 and related_id is null ")
                        .or().likeRight(AppInfo::getAppCode, "J-dos-")
                ;


            });
            appInfos = list(lqw);

        }
        List<Long> ids = appInfos.stream().map(item -> item.getId()).collect(Collectors.toList());
        Map<Long, List<InterfaceManage>> appId2Interfaces = null;
        {
            LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
            lqw.in(InterfaceManage::getAppId, ids);
            lqw.in(InterfaceManage::getType, InterfaceTypeEnum.JSF.getCode());
            lqw.eq(InterfaceManage::getYn, DataYnEnum.VALID.getCode());
            List<InterfaceManage> interfaces = interfaceManageService.list(lqw);
            appId2Interfaces = interfaces.stream().collect(Collectors.groupingBy(item -> item.getAppId()));

        }
        for (AppInfo appInfo : appInfos) {
            appInfo.setSite("cn");
            if (appInfo.getAppCode().startsWith("J-dos-")) {
                String appCode = appInfo.getAppCode().substring("J-dos-".length());
                log.info("jdos.update_app_code:appCode={}", appCode);
                appInfo.setJdosAppCode(appCode);
                updateById(appInfo);
            } else {
                List<InterfaceManage> interfaceManages = appId2Interfaces.get(appInfo.getId());
                if (interfaceManages == null) {
                    continue;
                }
                boolean saved = false;
                for (InterfaceManage interfaceManage : interfaceManages) {
                    if (saved) {
                        break;
                    }
                    QueryProviderRequest request = JSFArgBuilder.buildQueryProviderRequest();
                    request.setInterfaceName(interfaceManages.get(0).getServiceCode());
                    try {
                        com.jd.jsf.open.api.vo.Result<List<Server>> result = providerService.query(request);
                        log.info("jsf.query_provider:request={},response={}", request.getInterfaceName(), JsonUtils.toJSONString(result));
                        if (result.getData() != null && !result.getData().isEmpty()) {
                            String appName = getAppName(result.getData());
                            if (StringUtils.isNotBlank(appName)) {
                                log.info("jdos.update_jdos_app_code:appCode={}", appName);
                                appInfo.setJdosAppCode(appName);
                                saved = true;
                                updateById(appInfo);
                            }


                        }
                    } catch (Exception e) {
                        log.error("jsf.err_query_provider", e);
                    }
                }

            }
        }


    }

    private String getServerAppName(Server server) {
        String attrUrl = server.getAttrUrl();
        if (StringUtils.isNotBlank(attrUrl)) {
            attrUrl = attrUrl.replace("{", "").replace("}", "");
            List<String> list = StringHelper.split(attrUrl, ",");
            for (String s : list) {
                List<String> nameValue = StringHelper.split(s, "=");
                if (nameValue.size() == 2) {
                    if ("appName".equals(nameValue.get(0).trim())) {
                        String appName = nameValue.get(1);
                        if (appName != null && appName.startsWith("jdos_")) {
                            return appName.substring("jdos_".length());
                        }
                        break;
                    }
                }
            }
        }
        return null;
    }

    private String getAppName(List<Server> servers) {
        Map<String, Integer> appName2Count = new HashMap<>();
        for (Server server : servers) {
            String appName = getServerAppName(server);
            if (appName != null) {
                if (appName.contains("mock") || appName.contains("Mock")) {
                    continue;
                }
                Integer count = appName2Count.get(appName);
                if (count == null) {
                    count = 0;
                }
                count++;
                appName2Count.put(appName, count);
            }
        }
        String appName = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : appName2Count.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                appName = entry.getKey();
            }
        }
        return appName;
    }

    @Override
    public Boolean realRemoveApp(Long id) {
        AppInfo lastObj = this.getOne(Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getId, id).eq(AppInfo::getYn, DataYnEnum.VALID.getCode()));
        if (lastObj != null) {
            checkAuth(getAppInfoById(id));
        }
        return removeById(id);
    }

    @Override
    public void updateAppTenant(UpdateAppTenant appTenant) {
        List<AppInfo> appInfos = listByIds(appTenant.getIds());
        for (AppInfo appInfo : appInfos) {
            appInfo.setTenantId(appTenant.getTenantId());
            updateById(appInfo);
        }
    }

    @Override
    public AppInfoDTO findApp(Long id) {
        AppInfo appInfo = getAppInfoById(id);
        AppInfoDTO dto = AppInfoDTO.from(appInfo);
        final AppInfoDTO info = cjgHelper.getCjgComponetInfoByCode(appInfo.getAppCode());
        if (info != null) {
            dto.setCjgAppKey(info.getCjgAppKey());
            dto.setDevLanguage(info.getDevLanguage());
            dto.setAppTypeReqList(info.getAppTypeReqList());
        }
        if (dto.getOwner() != null) {
            UserVo vo = userHelper.getUserBaseInfoByUserName(dto.getOwner().get(0));
            if (vo != null) {
                dto.setDepartment(vo.getOrganizationFullName());
            }
        }
        return dto;
    }

    @Override
    public AppInfoDTO findAppByCode(String appCode) {
        AppInfo appInfo = this.getOne(Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getAppCode, appCode).eq(AppInfo::getYn, DataYnEnum.VALID.getCode()));
        AppInfoDTO dto = new AppInfoDTO();
        if (appInfo == null) {
            Long appId = syncCjgAppToLocal(appCode, false);
            if (appId == null) return dto;
            appInfo = getById(appId);

        } else if (configInfoService.isForceUpdateAppInfo()) {
            Long appId = syncCjgAppToLocal(appCode, false);
            if (appId == null){
                BeanUtils.copyProperties(appInfo, dto);
                dto.splitMembers(appInfo.getMembers());
                if (dto.getOwner() != null) {
                    UserVo vo = userHelper.getUserBaseInfoByUserName(dto.getOwner().get(0));
                    if (vo != null) {
                        dto.setDepartment(vo.getOrganizationFullName());
                    }
                }
                return dto;
            }
            appInfo = getById(appId);
        }
        BeanUtils.copyProperties(appInfo, dto);
        dto.splitMembers(appInfo.getMembers());
        if (dto.getOwner() != null) {
            UserVo vo = userHelper.getUserBaseInfoByUserName(dto.getOwner().get(0));
            if (vo != null) {
                dto.setDepartment(vo.getOrganizationFullName());
            }
        }
        final AppInfoDTO info = cjgHelper.getCjgComponetInfoByCode(appInfo.getAppCode());
        if (info != null) {
            dto.setCjgAppKey(info.getCjgAppKey());
        }
        return dto;
    }

    @Override
    public AppInfo findByJdosCode(String jdosAppCode) {
        LambdaQueryWrapper<AppInfo> lqw = Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getJdosAppCode, jdosAppCode).eq(AppInfo::getYn, DataYnEnum.VALID.getCode());
        List<AppInfo> list = list(lqw);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    private void appMemberSearchCondition(LambdaQueryWrapper<AppInfo> lqw, String erp) {
        if (StringUtils.isBlank(erp)) {
            return;
        }
        try {
            if (SafeUtil.sqlValidate(erp)) {
                throw new Exception("您发送请求中的参数中含有非法字符");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        lqw.apply("id in (select app_id from app_info_members where erp ={0} )", erp);
    }

    private void appDeptSearchCondition(LambdaQueryWrapper<AppInfo> lqw, String dept) {
        if (StringUtils.isBlank(dept)) {
            return;
        }
        String sql = "id in (select DISTINCT aim.app_id from app_info_members aim  left join user_info ui on aim.erp=ui.user_code where dept like '"+dept+"%')";
        lqw.apply(sql);
    }


    @Override
    public AppInfo findApp(String appCode) {
        AppInfo appInfo = this.getOne(Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getAppCode, appCode).eq(AppInfo::getYn, DataYnEnum.VALID.getCode()));
        return appInfo;
    }

    @Override
    public Boolean checkSecret(String appCode, String appSecret) {
        AppInfo obj = this.getOne(Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getAppCode, appCode).eq(AppInfo::getYn, DataYnEnum.VALID.getCode()));
        if (obj == null) {
            log.error("IAppInfoService.checkSecret execute getByCode return null, param={}>>>>>>>", appCode);
            return false;
        }
        return appSecret.equals(obj.getAppSecret());
    }

    public QueryAppResultDTO queryAppByCondition(QueryAppReqDTO query) {
        return queryAppByCondition(query, false);
    }

    /**
     * 接口市场app 查询拥有jdosCode的应用& 有接口的应用
     * @param query
     * @return
     */
    @Override
    public Page<AppInfoDTO> querySpaceAppByCondition(QueryAppReqDTO query,Integer queryType) {
        query.initPageParam(200);
        LambdaQueryWrapper<AppInfo> qw = Wrappers.<AppInfo>lambdaQuery().orderByDesc(AppInfo::getId);
        qw.eq(AppInfo::getYn, 1);
        qw.isNotNull(AppInfo::getJdosAppCode);
        if (StringUtils.isNotBlank(query.getName())) {
            qw.and(vs -> {
                vs.or().like(AppInfo::getAppName, query.getName()).or().like(AppInfo::getAppCode, query.getName()).or().like(AppInfo::getJdosAppCode, query.getName());
            });
        }
        qw.isNotNull(1==queryType,AppInfo::getCjgProductTrace);
        qw.isNotNull(2==queryType,AppInfo::getCjgBusinessDomainTrace);
        qw.likeRight(StringUtils.isNotBlank(query.getDept()), AppInfo::getDept, query.getDept());
        qw.like(StringUtils.isNotBlank(query.getCjgBusinessDomainTrace()), AppInfo::getCjgBusinessDomainTrace, query.getCjgBusinessDomainTrace());
        qw.like(StringUtils.isNotBlank(query.getCjgProductTrace()), AppInfo::getCjgProductTrace, query.getCjgProductTrace());
        qw.like(StringUtils.isNotBlank(query.getAdmin()), AppInfo::getMembers, query.getAdmin());
//        qw.and(child -> {
//            child.and(wrapper -> {
//                wrapper.like(StringUtils.isNotBlank(query.getAdmin()), AppInfo::getMembers, AppUserTypeEnum.JDOS_OWNER.getType() + "-" + query.getAdmin());
//            }).or(wrapper -> {
//                wrapper.like(StringUtils.isNotBlank(query.getAdmin()), AppInfo::getMembers, AppUserTypeEnum.JDOS_OWNER.getType() + "-" + query.getAdmin());
//            });
//        });

        qw.inSql(AppInfo::getId, "select distinct app_id from interface_manage where yn=1 and app_id is not null");
        Page<AppInfo> pageResult = this.page(new Page<>(query.getCurrentPage(), query.getPageSize()), qw);

        List<AppInfoDTO> records = pageResult.getRecords().stream().map(o -> {
            AppInfoDTO dto = new AppInfoDTO();
            BeanUtils.copyProperties(o, dto);
            dto.splitMembers(o.getMembers());
            return dto;
        }).collect(Collectors.toList());
        Page<AppInfoDTO> rest = new Page<>();
        rest.setRecords(records);
        rest.setCurrent(query.getCurrentPage());
        rest.setSize(query.getPageSize());
        rest.setTotal(pageResult.getTotal());
        return rest;
    }

    public QueryAppResultDTO queryAppByCondition(QueryAppReqDTO query, boolean hasNoApp) {
        query.initPageParam(1000);
        QueryAppResultDTO result = new QueryAppResultDTO();
        result.setCurrentPage(query.getCurrentPage());
        result.setPageSize(query.getPageSize());
        LambdaQueryWrapper<AppInfo> qw = Wrappers.<AppInfo>lambdaQuery();
        qw.eq(query.getId() != null, AppInfo::getId, query.getId());
        if (StringUtils.isNotBlank(query.getName())) {
            qw.and(vs -> {
                vs.or().like(AppInfo::getAppName, query.getName()).or().like(AppInfo::getAppCode, query.getName());
            });
        }
        qw.eq(StringUtils.isNotBlank(query.getAppCode()), AppInfo::getAppCode, query.getAppCode());
        qw.like(StringUtils.isNotBlank(query.getAppName()), AppInfo::getAppName, query.getAppName());
        qw.eq(StringUtils.isNotBlank(query.getAuthLevel()), AppInfo::getAuthLevel, query.getAuthLevel());
        qw.eq(StringUtils.isNotBlank(query.getAppType()), AppInfo::getAppType, query.getAppType());
        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());

        if (userRoleDTO.getJapiAdmin()) {
            //管理员可以看到所有数据。


        } else if (userRoleDTO.getDeptLeader()) {
            //部门负责人可以看到所属部门所有数据
            appDeptSearchCondition(qw, userRoleDTO.getDept());
        } else if (userRoleDTO.getJapiDepartment()) {
            //部门接口人，可以看到所属部门数据
            StringBuilder deptBuilder = new StringBuilder();
            String[] depts = userRoleDTO.getDept().split("-");
            if (depts.length > 5) {
                deptBuilder.append(depts[0]);
                deptBuilder.append("-");
                deptBuilder.append(depts[1]);
                deptBuilder.append("-");
                deptBuilder.append(depts[2]);
                deptBuilder.append("-");
                deptBuilder.append(depts[3]);
                deptBuilder.append("-");
                deptBuilder.append(depts[4]);
            } else {
                deptBuilder.append(userRoleDTO.getDept());
            }

            appDeptSearchCondition(qw, deptBuilder.toString());
        } else {
            //普通用户，只能看到自己是成员的数据
            appMemberSearchCondition(qw, query.getPin());
        }
        if(StringUtils.isNotEmpty(query.getDept())){
            appDeptSearchCondition(qw, query.getDept());
        }

//            boolean isAdmin = memberRelationService.checkTenantAdmin(UserSessionLocal.getUser().getUserId());
//        if (!isAdmin) {
//            appMemberSearchCondition(qw, query.getPin());
//        }
        qw.eq(StringUtils.isNotBlank(query.getTenantId()), AppInfo::getTenantId, query.getTenantId());
        qw.eq(AppInfo::getYn, DataYnEnum.VALID.getCode());
        qw.or(child->{
            child.eq(AppInfo::getAppCode, "J-dos-japi-demo")
                    .eq(AppInfo::getYn, DataYnEnum.VALID.getCode());
        });
        Page<AppInfo> pageResult = null;
//        if (hasNoApp) {
//            int pageSize = query.getPageSize() - 2;
//            long start = (query.getCurrentPage() - 1) * (query.getPageSize() - 2);
//            int size = count(qw);
//            if (size % pageSize == 0) {
//                size = size / pageSize + size;
//            } else {
//                size = size / pageSize + 1 + size;
//            }
//            if (start > 0) {
//                start = start - 1;
//            }
//            qw.select(AppInfo::getId, AppInfo::getAppCode, AppInfo::getAppSecret, AppInfo::getAppName, AppInfo::getMembers, AppInfo::getAuthLevel, AppInfo::getCreated, AppInfo::getModified, AppInfo::getModifier, AppInfo::getTenantId, AppInfo::getAppType);
//            String sql = "limit {0},{1}";
//            sql = sql.replace("{0}", start + "");
//            sql = sql.replace("{1}", query.getPageSize() - 1 + "");
//
//            qw.last(sql);
//            pageResult = new Page<>(start, query.getPageSize(), size);
//            pageResult.setRecords(this.list(qw));
//        } else {
            qw.select(AppInfo::getId, AppInfo::getAppCode, AppInfo::getAppSecret, AppInfo::getAppName, AppInfo::getMembers, AppInfo::getAuthLevel, AppInfo::getCreated, AppInfo::getModified, AppInfo::getModifier, AppInfo::getTenantId, AppInfo::getAppType);
            qw.last("ORDER BY CASE  WHEN app_code = 'J-dos-japi-demo' THEN 0  ELSE 1 END, id desc ");
            pageResult = this.page(new Page<>(query.getCurrentPage(), query.getPageSize() - 2), qw);
//        }
        if (pageResult != null && CollectionUtils.isNotEmpty(pageResult.getRecords())) {
            result.setTotalCnt(Long.valueOf(pageResult.getTotal()));
            result.setList(pageResult.getRecords().stream().map(o -> {
                AppInfoDTO dto = new AppInfoDTO();
                BeanUtils.copyProperties(o, dto);
                dto.splitMembers(o.getMembers());
                return dto;
            }).collect(Collectors.toList()));
        }


        return result;
    }

    @Override
    public QueryAppResultDTO queryHasNoApp(QueryAppReqDTO query) {
        boolean hasNoApp = false;
        if (StringUtils.isBlank(query.getAppCode())
                && StringUtils.isBlank(query.getAppName())
                && StringUtils.isBlank(query.getName())
        ) {
            hasNoApp = interfaceManageService.hasNoAppInterface();
        }
        QueryAppResultDTO result = queryAppByCondition(query, hasNoApp);
        if (hasNoApp) {
            AppInfoDTO appInfoDTO = new AppInfoDTO();
            appInfoDTO.setAppName(NO_APP_NAME);
            appInfoDTO.setId(0L);
            result.getList().add(0, appInfoDTO);
//            if (result.getList().size() > query.getPageSize()) {
//                result.getList().remove(result.getList().size() - 1);
//            }
        }

        return result;
    }


    public List<AppAndSecret> searchNameNotSameApps(String search, String erp){
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();

        lqw.eq(AppInfo::getYn, 1);
        appMemberSearchCondition(lqw, erp);
        if (StringUtils.isNotBlank(search)) {
            lqw.and(vs -> {
                vs.or().like(AppInfo::getAppName, search).or().like(AppInfo::getAppCode, search);
            });
        }else{

        }

        /*lqw.not(child->{
            child.likeRight(AppInfo::getAppCode, "J-dos-");
        });*/



        Page<AppInfo> page = page(new Page<>(1, 1000), lqw);
        List<AppAndSecret> ret = page.getRecords().stream().map(vs -> {
            AppAndSecret result = new AppAndSecret();
            BeanUtils.copyProperties(vs, result);
            result.setAppCode(vs.getAppCode());
            result.setAppName(vs.getAppName());
            result.setJdosAppCode(vs.getJdosAppCode());
            result.setAppSecret(vs.getAppSecret());
            return result;
        }).collect(Collectors.toList());
        return ret;
    }



    public List<AppAndSecret> searchNoJdosApps(String search, String erp){
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();

        lqw.eq(AppInfo::getYn, 1);
        appMemberSearchCondition(lqw, erp);
        if (StringUtils.isNotBlank(search)) {
            lqw.and(vs -> {
                vs.or().like(AppInfo::getAppName, search).or().like(AppInfo::getAppCode, search);
            });
        }else{

        }
        lqw.isNull(AppInfo::getJdosAppCode);

        Page<AppInfo> page = page(new Page<>(1, 50), lqw);
        List<AppAndSecret> ret = page.getRecords().stream().map(vs -> {
            AppAndSecret result = new AppAndSecret();
            BeanUtils.copyProperties(vs, result);
            result.setAppCode(vs.getAppCode());
            result.setAppName(vs.getAppName());
            result.setJdosAppCode(vs.getJdosAppCode());
            result.setAppSecret(vs.getAppSecret());
            return result;
        }).collect(Collectors.toList());
        return ret;
    }
    @Override
    public List<AppSearchResult> searchApp(String app, Long id, int onlySelf, Integer includeNoApp) {
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();

        lqw.eq(AppInfo::getYn, 1);
        lqw.eq(id != null, AppInfo::getId, id);
        if (onlySelf == 1) {
            appMemberSearchCondition(lqw, UserSessionLocal.getUser().getUserId());
        } else {
            lqw.inSql(AppInfo::getId, "select app_id from interface_manage");
        }
        if (StringUtils.isNotBlank(app)) {
            lqw.and(vs -> {
                vs.or().like(AppInfo::getAppName, app).or().like(AppInfo::getAppCode, app);
            });
        }

        Page<AppInfo> page = page(new Page<>(1, 20), lqw);
        List<AppSearchResult> ret = page.getRecords().stream().map(vs -> {
            AppSearchResult result = new AppSearchResult();
            BeanUtils.copyProperties(vs, result);
            result.setAppCode(vs.getAppCode());
            result.setAppName(vs.getAppName());
            return result;
        }).collect(Collectors.toList());
        boolean queryNoApp = false;
        if (includeNoApp == null || includeNoApp == 1) {
            if ((onlySelf == 1 && StringUtils.isBlank(app) && id == null)) {
                queryNoApp = true;
            }

        }
        if (queryNoApp) {
            LambdaQueryWrapper<AppInfo> demo = new LambdaQueryWrapper<>();
            demo.eq(AppInfo::getYn, 1);
            demo.eq(AppInfo::getAppCode, "J-dos-japi-demo");
            AppInfo demoInfo = getOne(demo);
            AppSearchResult demott = new AppSearchResult();
            demott.setId(demoInfo.getId());
            demott.setAppName(demoInfo.getAppName());
            demott.setAppCode("J-dos-japi-demo");
            boolean hasNoAppInterface = interfaceManageService.hasNoAppInterface();
            if (hasNoAppInterface) {
                AppSearchResult result = new AppSearchResult();
                result.setId(0L);
                result.setAppName(NO_APP_NAME);
                ret.add(0, result);
                ret.add(1, demott);
            } else {
                ret.add(0, demott);
            }
        }
        return ret;
    }

    /**
     * 模糊查询藏经阁应用
     *
     * @param query
     * @return
     */
    @Override
    public QueryAppResultDTO queryImportSysApp(QueryAppReqDTO query) {
        query.initPageParam(30);
        QueryAppResultDTO resultDTO = cjgHelper.queryComponent(query);
        return resultDTO;
    }

    @Override
    public List<AppInfo> queryDjAppByPrefix(String prefix) {
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AppInfo::getYn, 1);
        lqw.likeRight(AppInfo::getAppCode, prefix);
        return list(lqw);
    }

    @Override
    public List<AppInfo> queryBeanApps(String name) {
        String userId = UserSessionLocal.getUser().getUserId();
        LambdaQueryWrapper<AppInfo> qw = new LambdaQueryWrapper<>();
        appMemberSearchCondition(qw, userId);
        qw.eq(AppInfo::getYn, DataYnEnum.VALID.getCode());
/*        qw.like(AppInfo::getAppCode,name);
        qw.like(AppInfo::getAppName,name);*/
        if (StringUtils.isNotBlank(name)) {
            qw.and(vs -> {
                vs.or().like(AppInfo::getAppName, name).or().like(AppInfo::getAppCode, name);
            });
        }
        qw.inSql(AppInfo::getId, "select app_id from interface_manage where type=9 and app_id is not null");
        Page<AppInfo> pageData = page(new Page<>(0, 20), qw);
        return pageData.getRecords();
    }

    /**
     * 根据id查已存在的对象数据
     *
     * @param id
     * @return
     */
    private AppInfo getAppInfoById(Long id) {
        AppInfo lastObj = this.getOne(Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getId, id).eq(AppInfo::getYn, DataYnEnum.VALID.getCode()));
        if (lastObj == null) {
            throw new BizException("应用不存在!");
        }
        return lastObj;
    }


    private void checkAuth(AppInfo info) {
        if (UserSessionLocal.getUser().getUserId() == null) return;
        if ("wangjingfang3".equals(UserSessionLocal.getUser().getUserId())) {
            return;
        }
        //TODO 管理员 直接返回
        if (UserSessionLocal.getUser().getUserId() != null && info.getMembers().indexOf((UserSessionLocal.getUser().getUserId() + ",")) == -1
        ) {
            throw new BizException("无应用操作权限!");
        }

    }


    /**
     * 同步更新藏经阁
     *
     * @param updateObj
     * @param lastObj
     * @return
     */
    private boolean syncCjgInfo(AppInfoDTO updateObj, AppInfoDTO lastObj) {
       /* if(!updateObj.getAppName().equals(lastObj.getAppName())
                ||!ListUtils.isEqualList(updateObj.getOwner(),lastObj.getOwner())
                    ||!ListUtils.isEqualList(updateObj.getProductor(),lastObj.getProductor())
                         ||!ListUtils.isEqualList(updateObj.getTester(),lastObj.getTester())){*/
        return cjgHelper.updateComponentByCode(updateObj);
        //}
        //return true;
    }

    /**
     * 更新该APP下接口的部门信息
     *
     * @param app
     */
    private void updateInterfaceInfoByAppInfo(AppInfo app) {
        try {
            String deptName = getDeptNameFoyAppMember(app);
            if (StringUtils.isBlank(deptName)) {
                log.error("AppInfoServiceImpl.updateInterfaceInfoByAppInfo deptName is blank!!! app:{}", app);
                return;
            }
            // 更新应用下接口的部门信息
            List<InterfaceManage> interfaceManages = interfaceManageService.queryListByAppId(app.getId());
            for (InterfaceManage interfaceManage : interfaceManages) {
                // 更新部门信息
                interfaceManage.setDeptName(deptName);
            }
            if (!interfaceManages.isEmpty()) {
                interfaceManageService.batchUpdateInterfaceInfo(interfaceManages);
            }

        } catch (Exception e) {
            log.error("AppInfoServiceImpl.updateInterfaceInfoByAppInfo error!! appInfo:{}", app, e);
        }
    }

    /**
     * 查询应用负责人的部门信息
     *
     * @param app
     * @return
     */
    public String getDeptNameFoyAppMember(AppInfo app) {
        // 查询应用主负责人的部门信息
        String deptName = null;
        if (Objects.isNull(app) || StringUtils.isBlank(app.getMembers())) {
            return deptName;
        }
        List<String> owners = AppUserTypeEnum.OWNER.splitErps(app.getMembers(), "-", ",");
        if (CollectionUtils.isNotEmpty(owners)) {
            UserVo vo = userHelper.getUserBaseInfoByUserName(owners.get(0));
            if (vo != null) {
                deptName = vo.getOrganizationFullName();
            }
        }
        return deptName;
    }


    /**
     * 查询应用负责人的部门信息
     *
     * @param appId
     * @return
     */
    public String getDeptNameFoyAppMember(Long appId) {
        // 查询应用主负责人的部门信息
        String deptName = null;
        try {
            AppInfo app = getAppInfoById(appId);
            if (Objects.isNull(app) || StringUtils.isBlank(app.getMembers())) {
                return deptName;
            }
            List<String> owners = AppUserTypeEnum.OWNER.splitErps(app.getMembers(), "-", ",");
            if (CollectionUtils.isNotEmpty(owners)) {
                UserVo vo = userHelper.getUserBaseInfoByUserName(owners.get(0));
                if (vo != null) {
                    deptName = vo.getOrganizationFullName();
                }
            }
        } catch (Exception e) {
            log.error("AppInfoServiceImpl.getDeptNameFoyAppMember ERROR!!! appId:{}", appId);
        }
        return deptName;
    }


    public void sysAppInfoMem(String appCode) {
        LambdaQueryWrapper<AppInfo> qw = Wrappers.<AppInfo>lambdaQuery().orderByDesc(AppInfo::getId);
        if (StringUtils.isNotBlank(appCode)) {
            qw.eq(AppInfo::getAppCode, appCode);
        }
        int pageIndex = 1;

        while (true) {
            Page<AppInfo> pageResult = this.page(new Page(pageIndex, 1000), qw);
            if (pageResult == null || CollectionUtils.isEmpty(pageResult.getRecords())) {
                break;
            }
            List<AppInfo> records = pageResult.getRecords();
            if (CollectionUtils.isNotEmpty(records)) {
                records.stream().forEach(item -> {

                    appInfoMembersService.saveMembersByStr(item, item.getAppCode());
                });
            }
            pageIndex++;
        }

    }

    @Override
    public void updateAllAppTrace(String cookie) {
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AppInfo::getYn, 1);
        lqw.isNotNull(AppInfo::getJdosAppCode);
        List<AppInfo> appInfos = list(lqw);
        for (AppInfo appInfo : appInfos) {
            try {
                updateAppTrace(appInfo, cookie);
            } catch (Exception e) {
                log.error("app.err_update_trace:appCode={}", appInfo.getAppCode(), e);
            }
        }
    }

    @Override
    public void updateAppTrace(AppInfo appInfo, String cookie) {
        Guard.notEmpty(appInfo.getJdosAppCode(), "jdos应用编码不能为空");
        List<String> traceCodes = cjgApiImporter.queryAppDomains(cookie, appInfo.getJdosAppCode());
        if (!traceCodes.isEmpty()) {
            String result = StringHelper.join(traceCodes, ",");
            result = "," + result;
            appInfo.setCjgBusinessDomainTrace(result);
            updateById(appInfo);
            List<InterfaceManage> interfaces = interfaceManageService.getAppInterfaces(appInfo.getId());
            for (InterfaceManage anInterface : interfaces) {
                anInterface.setCjgBusinessDomainTrace(result);
                LambdaUpdateWrapper<InterfaceManage> lqw = new LambdaUpdateWrapper<>();
                lqw.eq(InterfaceManage::getId, anInterface.getId());
                lqw.set(InterfaceManage::getCjgBusinessDomainTrace, result);
                interfaceManageService.update(lqw);
            }
        }
    }

    @Override
    public void updateProductTrace(AppInfo appInfo) {
//        Guard.notEmpty(appInfo.getJdosAppCode(), "jdos应用编码不能为空");
//        List<JagileAppReq> reqs = new ArrayList<>();
//        JagileAppReq req = new JagileAppReq();
//        //租户PS:JDT JDD
//        req.setTenant(JagileJdosSiteEnum.getCodeBySite(appInfo.getSite()));
//        req.setAppAlias(appInfo.getJdosAppCode());
//        reqs.add(req);
//        //获取productTrace
//        Result<List<AppProductPanoramaVo>> vos = graphProductPanoramaProvider.appProductPanorama(reqs);
//        log.info("测试graphProductPanoramaProvider--{}",JSONObject.toJSONString(vos));
//
//        String productTraceText="";
//        if (CollectionUtils.isNotEmpty(vos.getModel())) {
//            List<String> productTrace = vos.getModel().stream().map(AppProductPanoramaVo::getTrace).collect(Collectors.toList());
//            if (CollectionUtils.isNotEmpty(productTrace)) {
//                productTraceText = StringHelper.join(productTrace, ",");
//                productTraceText = "," + productTraceText;
//                appInfo.setCjgProductTrace(productTraceText);
//            }
//        }
//        if (StringUtils.isNotBlank(productTraceText)) {
//            LambdaUpdateWrapper<AppInfo> wrapper = new LambdaUpdateWrapper<>();
//            wrapper.eq(AppInfo::getId, appInfo.getId());
//            wrapper.eq(AppInfo::getYn, 1);
//            wrapper.set(AppInfo::getCjgProductTrace, productTraceText);
//            update(wrapper);
//
//            List<InterfaceManage> interfaces = interfaceManageService.getAppInterfaces(appInfo.getId());
//            for (InterfaceManage anInterface : interfaces) {
//                LambdaUpdateWrapper<InterfaceManage> lqw = new LambdaUpdateWrapper<>();
//                lqw.eq(InterfaceManage::getId, anInterface.getId());
//                lqw.set(InterfaceManage::getCjgProductTrace, productTraceText);
//                interfaceManageService.update(lqw);
//            }
//        }
            }


    @Override
    public boolean isMember(Long id) {
        AppInfo appInfo = getById(id);
        JdosAppInfoDto dto = new JdosAppInfoDto();
        BeanUtils.copyProperties(appInfo, dto);
        dto.splitMembers(appInfo.getMembers());
        Set<String> allMembers = new HashSet<>();
        allMembers.add(dto.getOwner());
        Optional.of(dto.getJdosMembers()).ifPresent(item -> allMembers.addAll(item));
        Optional.of(dto.getMembers()).ifPresent(item -> allMembers.addAll(item));
        return allMembers.contains(UserSessionLocal.getUser().getUserId());
    }


    @Override
    public List<AppInfo> InitByJdosCode(String jdosAppCode, String erp) {
        LambdaQueryWrapper<AppInfo> lqw = Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getJdosAppCode, jdosAppCode).eq(AppInfo::getYn, DataYnEnum.VALID.getCode());
        List<AppInfo> appInfoList = list(lqw);

        if (org.apache.commons.collections4.CollectionUtils.isEmpty(appInfoList)) {
            //不存在，就创建。
            appInfoList = initJdosApp(jdosAppCode, erp);


        } else {
            for (AppInfo appInfo : appInfoList) {
                if (!appInfo.getMembers().contains(erp)) {
                    appInfo.setMembers(appInfo.getMembers() + ",10-" + erp);
                    updateById(appInfo);
                }

            }

        }

        return appInfoList;
    }

    private List<AppInfo> initJdosApp(String jdosAppCode, String erp) {
        List<AppInfo> appInfoList;
        AppMembers appMembers = getJdosAppMembers(jdosAppCode);
        AppInfoDTO dto = new AppInfoDTO();
        if(StringUtils.isNotBlank(erp)){
            if (!appMembers.getJdosMembers().contains(erp)) {
                appMembers.getJdosMembers().add(erp);
            }
        }

        dto.setMember(appMembers.getJdosMembers().stream().collect(Collectors.toList()));
        dto.setJdosMembers(appMembers.getJdosMembers().stream().collect(Collectors.toList()));
        dto.setOwner(Collections.singletonList(appMembers.getOwner()));
        dto.setTester(Collections.singletonList(appMembers.getOwner()));
        dto.setProductor(Collections.singletonList(appMembers.getOwner()));
        dto.setJdosOwner(appMembers.getOwner());
        dto.setAppCode("J-dos-" + jdosAppCode);
        dto.setAppName(jdosAppCode);
        //dto.setAuthLevel("0");
        dto.setSite("cn");
        Long id = addApp(dto);
        AppInfo appInfo = getById(id);
        appInfoList = Lists.newArrayList(appInfo);
        return appInfoList;
    }

    /**
     * 通过jdosCode 初始化JApi应用
     * @param jdosAppCode
     * @return
     */
    public AppInfo InitByJdosCode(String jdosAppCode) {
        UserInfoInSession user = UserSessionLocal.getUser();
        if(StringUtils.isEmpty(user.getTenantId())){
            user.setTenantId("up_jd");
        }
        String appCode = "J-dos-" + jdosAppCode;
        LambdaQueryWrapper<AppInfo> lqw = Wrappers.<AppInfo>lambdaQuery().eq(AppInfo::getAppCode,appCode ).eq(AppInfo::getYn, DataYnEnum.VALID.getCode());
        List<AppInfo> appInfoList = list(lqw);
        if (CollectionUtils.isEmpty(appInfoList)) {
            //不存在，就创建。
            AppMembers appMembers = getJdosAppMembers(jdosAppCode);
            AppInfoDTO dto = new AppInfoDTO();
            dto.setMember(appMembers.getJdosMembers().stream().collect(Collectors.toList()));
            dto.setJdosMembers(appMembers.getJdosMembers().stream().collect(Collectors.toList()));
            dto.setOwner(Collections.singletonList(appMembers.getOwner()));
            dto.setTester(Collections.singletonList(appMembers.getOwner()));
            dto.setProductor(Collections.singletonList(appMembers.getOwner()));
            dto.setJdosOwner(appMembers.getOwner());
            dto.setAppCode("J-dos-" + jdosAppCode);
            dto.setAppName(jdosAppCode);
            //dto.setAuthLevel("0");
            dto.setSite("cn");
            Long id = addApp(dto);
            AppInfo appInfo = getById(id);
            appInfoList = Lists.newArrayList(appInfo);
        }
        if(CollectionUtils.isNotEmpty(appInfoList)){
            return appInfoList.get(0);
        }else{
            return null;
        }
    }

    @Override
    public List<AppInfo> queryAppInfosByJdosCoeds(List<String> jdosCoedes) {
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AppInfo::getYn, 1);
        lqw.isNotNull(AppInfo::getJdosAppCode);
        lqw.in(AppInfo::getJdosAppCode,jdosCoedes);
        return list(lqw);
    }

    @Override
    public List<AppInfo> queryAppInfoListByAppCoeds(List<String> appCodes){
        LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(AppInfo::getYn, 1);
        lqw.isNotNull(AppInfo::getJdosAppCode);
        lqw.in(AppInfo::getAppCode,appCodes);
        return list(lqw);
    }

    /**
     * 获取要求下的应用列表
     *
     * @param requirementId
     */
    @Override
    public List<AppInfo> getAppByRequirementId(Long requirementId) {
        return appInfoMapper.selectByRequirementId(requirementId);
    }

    public void updateAppDepts(){
        long current = 1;
        long size = 1000;
        while (true){
            LambdaQueryWrapper<AppInfo> lqw = new LambdaQueryWrapper<>();
            lqw.eq(AppInfo::getYn,1);
            Page<AppInfo> page = page(new Page<>(current, size), lqw);
            List<AppInfo> records = page.getRecords();
            current++;
            if(records == null || records.isEmpty()){
                return;
            }
            updateAppDept(records);

        }
    }
    private String getAppOwner(AppInfo app){
        AppInfoDTO appDto = AppInfoDTO.from(app);
        String owner = appDto.getJdosOwner();
        if(StringUtils.isBlank(owner) ){
            if(!ObjectHelper.isEmpty(appDto.getOwner())){
                owner = appDto.getOwner().get(0);
            }

        }
        return owner;
    }
    public void updateAppDept(List<AppInfo> apps){

        Set<String> userCodes = apps.stream().map(item -> getAppOwner(item))
                .filter(item -> StringUtils.isNotBlank(item)).collect(Collectors.toSet());

        if(userCodes.isEmpty()){
            return;
        }
        List<UserInfo> users = userInfoService.getUsers(userCodes.stream().collect(Collectors.toList()));
        Map<String, List<UserInfo>> userCode2Info = users.stream().collect(Collectors.groupingBy(UserInfo::getUserCode));

        for (AppInfo app : apps) {
            String appOwner = getAppOwner(app);
            if(StringUtils.isNotBlank(appOwner)){
                List<UserInfo> userInfos = userCode2Info.get(appOwner);
                if(userInfos != null){
                    UserInfo user = userInfos.get(0);
                    if(user == null||StringUtils.isEmpty(user.getDept())) continue;

                    LambdaUpdateWrapper<AppInfo> luw = new LambdaUpdateWrapper<>();
                    luw.set(AppInfo::getDept,user.getDept());
                    luw.eq(AppInfo::getId,app.getId());
                    update(luw);
                }
            }
        }


    }






    public boolean syncOwnerAndProductorFromCjg(AppInfo appInfo) {
        Result<ComponentInfoVo> result = cjgBusInterfaceRpcService.getComponentInfoByCode(appInfo.getAppCode());
        log.info("app.get_cjg_app_info:cjgAppCode={},result={}", appInfo.getAppCode(), JsonUtils.toJSONString(result));
        ComponentInfoVo vo = result.getModel();

        if (result == null || vo == null) {
            log.info("app.ignore_sync_app_owner:appCode={}", appInfo.getAppCode());
            return false;
        }

        AppInfoDTO dto = new AppInfoDTO();
        String before = appInfo.getMembers();
        dto.splitMembers(appInfo.getMembers());
        {
            Set<String> productor = new HashSet<>();
            productor.addAll(vo.getProductManagerList());
            productor.addAll(dto.getProductor());
            dto.setProductor(productor.stream().collect(Collectors.toList()));
        }
        {
            dto.setOwner(Collections.singletonList(vo.getProjectManager()));
        }
        log.info("app.update_app_productor:appCode={},productor={},owner={}", appInfo.getAppCode(), dto.getProductor(), dto.getOwner());
        appInfo.setMembers(dto.buildMembers());
        log.info("app.update_app_members:appCode={},before={},after={}", appInfo.getAppCode(), before, appInfo.getMembers());
        updateById(appInfo);
        appInfoMembersService.saveMembersByStr(appInfo, appInfo.getAppCode());
        return true;
    }


    public void resetAllMember() {
        LambdaQueryWrapper<AppInfo> qw = Wrappers.<AppInfo>lambdaQuery().orderByAsc(AppInfo::getId);

        int pageIndex = 1;
        long start = System.currentTimeMillis();
        log.info("app.begin_sync_app_member");
        while (true) {
            Page<AppInfo> pageResult = this.page(new Page(pageIndex, 1000), qw);
            if (pageResult == null || CollectionUtils.isEmpty(pageResult.getRecords())) {
                break;
            }
            List<AppInfo> records = pageResult.getRecords();
            if (CollectionUtils.isNotEmpty(records)) {
                records.stream().forEach(item -> {
                    try {
                        changeOwnerToJdosOwner(item);
                        syncOwnerAndProductorFromCjg(item);
                    } catch (Exception e) {
                        log.error("app.err_change_members:appCode={}", item.getAppCode(), e);
                    }

                });
            }
            pageIndex++;
        }
        log.info("app.sync_app_member_end:cost={}", System.currentTimeMillis() - start);
    }



    public void changeOwnerToJdosOwner(AppInfo appInfo) {
        String members = appInfo.getMembers();
        AppInfoDTO dto = new AppInfoDTO();
        dto.splitMembers(members);
        if (!ObjectHelper.isEmpty(dto.getOwner())) {

            dto.setJdosOwner(dto.getOwner().get(0));
            log.info("app.sync_jdos_app_members:appCode={},jdosAppMembers={}", appInfo.getAppCode(), dto.getJdosOwner());
        }
        appInfo.setMembers(dto.buildMembers());
        updateById(appInfo);
        appInfoMembersService.saveMembersByStr(appInfo, appInfo.getAppCode());
    }

}
