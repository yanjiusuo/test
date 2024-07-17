package com.jd.workflow.console.service.doc.importer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.StatusResult;
import com.jd.workflow.console.base.UserInfoInSession;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.config.dao.MetaContextHelper;
import com.jd.workflow.console.dao.mapper.InterfaceManageMapper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.MethodGroupTreeModel;
import com.jd.workflow.console.dto.doc.GroupSortModel;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.helper.CjgHelper;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IHttpAuthApplyService;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.doc.importer.dto.JApiProjectInfo;
import com.jd.workflow.console.service.doc.importer.dto.JApiProjectOwner;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.impl.InterfaceMethodGroupServiceImpl;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.metrics.client.RequestClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * 初始化japi的流程数据，初始化步骤如下：
 * 1. 初始化j-api的用户以及菜单信息
 * 2. 清除原有的interfaceManage的relatedId
 * 3. 更新j-api的interfaceManage的relatedId
 * <p>
 * 最后一步:清除无效的应用
 */
@Service
@Slf4j
public class JapiDataSyncService {
    public static final String CJG_API_URL = "http://cjg-api.jd.com";
    @Autowired
    RequirementInterfaceGroupService requirementInterfaceGroupService;
    @Autowired
    RequirementInfoService requirementInfoService;
    @Autowired
    IAppInfoService appInfoService;
    @Resource(name = "docThreadExecutor")
    ScheduledThreadPoolExecutor scheduleService;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IHttpAuthApplyService httpAuthApplyService;
    @Autowired
    IMemberRelationService memberRelationService;

    @Autowired
    JapiHttpDataImporter japiHttpDataImporter;
@Autowired
    InterfaceMethodGroupServiceImpl interfaceMethodGroupService;

    @Autowired
    InterfaceManageMapper interfaceManageMapper;
    RequestClient requestClient;

    @Autowired
    CjgHelper cjgHelper;

    @PostConstruct
    public void init() {
        requestClient = new RequestClient();
    }

    //删除无效的app
    public void syncAppInfo(String cookie) {
        List<AppInfo> appInfos = appInfoService.queryDjAppByPrefix(JapiHttpDataImporter.JAPI_APP_PREFIX);

        Set<String> httpAuthApps = getAllHttpAuthApps();
        List<String> jsfApps = getAllJsfAuthApp();
        Map<Long, List<InterfaceManage>> appId2Interfaces = getAppAndInterfaces();
        appInfos.forEach(item -> {
            if (!item.getAppCode().startsWith(JapiHttpDataImporter.JAPI_APP_PREFIX)) {
                throw new BizException("删除app失败：不是japi的app").param("appCode", item.getAppCode());
            }
            final List<InterfaceManage> interfaceManages = appId2Interfaces.get(item.getId());
            if (canDeleteApp(item, appId2Interfaces, httpAuthApps, jsfApps)) {
                deleteApp(item.getAppCode(), cookie);
                appInfoService.removeById(item.getId());
                if (!ObjectHelper.isEmpty(interfaceManages)) {
                    LambdaUpdateWrapper<InterfaceManage> luw = new LambdaUpdateWrapper<>();

                    luw.eq(InterfaceManage::getId, interfaceManages.get(0).getId());
                    luw.set(InterfaceManage::getAppId, null);
                    interfaceManageService.update(luw);
                }

            }
        });
    }

    public void deleteApp(String appCode, String cookie) {
        String url = CJG_API_URL + "/api/component/deleteComponent?componentId=";
        final AppInfoDTO cjgComponetInfoByCode = cjgHelper.getCjgComponetInfoByCode(appCode);
        if (cjgComponetInfoByCode == null) return;
        String cjgAppKey = cjgComponetInfoByCode.getCjgAppKey();
        Map<String, Object> headers = new HashMap<>();
        headers.put("Cookie", "sso.jd.com=" + cookie);
        final StatusResult<Map<String, Object>> result = requestClient.get(url + cjgAppKey, null, headers, new TypeReference<StatusResult<Map<String, Object>>>() {
        });
        if (result.getStatus() != 200) {
            throw new BizException("删除app失败：" + result.getMessage());
        }
    }

    // 清除无效的接口
    public void clearInterfaceRelatedId() {
        final int result =
                interfaceManageMapper.clearInvalidRelatedId();
        log.info("interface.clear_related_id_count:result={}", result);
        //interfaceManageService.updateBatchById(interfaceManages);
    }

    public void updateInterfaceManageRelatedId() {
        Map<Long, List<InterfaceManage>> appId2Interfaces = getAppAndInterfaces();
        List<AppInfo> appInfos = appInfoService.queryDjAppByPrefix(JapiHttpDataImporter.JAPI_APP_PREFIX);
        appInfos.forEach(item -> {
            String relatedId = item.getAppCode().substring(JapiHttpDataImporter.JAPI_APP_PREFIX.length());
            List<InterfaceManage> interfaceManages = appId2Interfaces.get(item.getId());
            if (interfaceManages == null) {
                return;
            }
            interfaceManages.forEach(interfaceManage -> {
                if ("japiDefault".equals(interfaceManage.getServiceCode())) {
                    interfaceManage.setRelatedId(Long.valueOf(relatedId));
                    interfaceManageService.updateById(interfaceManage);
                }

            });
        });
    }

    private boolean canDeleteApp(AppInfo app, Map<Long, List<InterfaceManage>> appId2Interfaces, Set<String> httpAuthApps, List<String> jsfApps) {
        if (httpAuthApps.contains(app.getAppCode())) {
            return false;
        }
        if (jsfApps.contains(app.getAppCode())) {
            return false;
        }
        List<InterfaceManage> interfaceManages = appId2Interfaces.get(app.getId());
        if (interfaceManages == null || interfaceManages.isEmpty()) {
            return true;
        }
        if (interfaceManages.size() == 1
                && ("japiDefault".equals(interfaceManages.get(0).getServiceCode())
                || interfaceManages.get(0).getServiceCode().startsWith("japi_"))

        ) {
            return true;
        }


        return false;


    }

    public Map<Long, List<InterfaceManage>> getAppAndInterfaces() {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.select(InterfaceManage::getId, InterfaceManage::getAppId, InterfaceManage::getYn, InterfaceManage::getRelatedId, InterfaceManage::getServiceCode);
        lqw.eq(InterfaceManage::getYn, 1);
        lqw.inSql(InterfaceManage::getAppId, "select id from app_info where app_code like '" + JapiHttpDataImporter.JAPI_APP_PREFIX + "%'");
        List<InterfaceManage> interfaceManages = interfaceManageService.list(lqw);
        return interfaceManages.stream().collect(Collectors.groupingBy(InterfaceManage::getAppId));
    }


    public Set<String> getAllHttpAuthApps() {
        LambdaQueryWrapper<HttpAuthApply> lqw = new LambdaQueryWrapper<>();
        lqw.select(HttpAuthApply::getAppCode, HttpAuthApply::getCallAppCode);
        final List<HttpAuthApply> applyList = httpAuthApplyService.list(lqw);
        Set<String> appCodes = applyList.stream().map(item -> {
            List<String> list = new ArrayList<>();
            list.add(item.getAppCode());
            list.add(item.getCallAppCode());
            return list;
        }).flatMap(list -> {
            return list.stream();
        }).collect(Collectors.toSet());
        return appCodes;
    }

    public List<InterfaceManage> getAllJapiInterfaces() {
        return null;
    }

    public List<String> getAllJsfAuthApp() {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.select(InterfaceManage::getCjgAppId);
        lqw.isNotNull(InterfaceManage::getCjgAppId);
        return interfaceManageService.list(lqw).stream().map(item -> item.getCjgAppId()).collect(Collectors.toList());
    }

    public void syncRequirementMembers(JApiProjectInfo projectInfo, Long requirementId) {
        List<MemberRelation> existMembers = requirementInfoService.getMembers(requirementId);
        List<MemberRelation> newMembers = new ArrayList<>();
        for (JApiProjectOwner partner : projectInfo.getPartners()) {
            MemberRelation relation = partner.toRelation();
            if (relation == null) {
                continue;
            }
            relation.setResourceType(ResourceTypeEnum.PRD_GROUP.getCode());
            relation.setResourceId(requirementId);
            relation.setCreated(new Date());
            relation.setCreator("syncRequirementMember");
            newMembers.add(relation);
        }
        japiHttpDataImporter.mergeMembers(existMembers, newMembers);
    }
     private  void changeGroupTree(MethodGroupTreeModel methodGroupTreeModel,InterfaceManage manage){
         List<GroupSortModel> groupSortModels = methodGroupTreeModel.allGroups();
         List<Long> groupIds = groupSortModels.stream().map(item -> item.getId()).collect(Collectors.toList());

          List<InterfaceMethodGroup> oldGroups = interfaceMethodGroupService.listByIds(groupIds);
            Map<Long, InterfaceMethodGroup> oldGroupMap = oldGroups.stream().collect(Collectors.toMap(InterfaceMethodGroup::getId, item -> item));
            List<InterfaceMethodGroup> newGroups = new ArrayList<>();
            Map<Long,InterfaceMethodGroup> id2NewGroups = new HashMap<>();
            for (GroupSortModel groupSortModel : groupSortModels) {
                InterfaceMethodGroup oldGroup = oldGroupMap.get(groupSortModel.getId());
                if (oldGroup != null) {

                    InterfaceMethodGroup  newGroup = new InterfaceMethodGroup();
                    newGroup.setName(oldGroup.getName());
                    newGroup.setEnName(oldGroup.getEnName());
                    newGroup.setType(GroupTypeEnum.PRD.getCode());
                    newGroup.setInterfaceId(oldGroup.getId());
                    newGroup.setRelatedId(oldGroup.getId());
                    id2NewGroups.put(oldGroup.getId(),newGroup);
                    newGroups.add(newGroup);
                }else{
                    InterfaceMethodGroup  newGroup = new InterfaceMethodGroup();
                    newGroup.setName(groupSortModel.getName());
                    newGroup.setEnName(groupSortModel.getEnName());
                    newGroup.setType(GroupTypeEnum.PRD.getCode());
                    newGroup.setInterfaceId(groupSortModel.getId());
                    newGroup.setRelatedId(groupSortModel.getId());
                    id2NewGroups.put(groupSortModel.getId(),newGroup);
                    newGroups.add(newGroup);
                }
            }
            if(!newGroups.isEmpty()){
                interfaceMethodGroupService.saveBatch(newGroups);

            }
             for (GroupSortModel groupSortModel : groupSortModels) {
                 groupSortModel.setId(id2NewGroups.get(groupSortModel.getId()).getId());
             }
     }
    public void syncRequirementTree(Long japiProjectId,Long interfaceId){
        InterfaceManage manage = interfaceManageService.getById(interfaceId);
        RequirementInfo requirementinfo = requirementInfoService.getByJapiId(manage.getId());
        RequirementInterfaceGroup requireInterfaceGroup = null;
        if (requirementinfo == null) {
            requirementinfo = requirementInfoService.newJapiRequirement(manage.getId(), manage.getName());
            requirementInfoService.save(requirementinfo);
        } else {
            requireInterfaceGroup = requirementInterfaceGroupService.getByRequirementIdAndInterfaceId(requirementinfo.getId(), manage.getId());
        }
        changeGroupTree(manage.getSortGroupTree(),manage);
        if (requireInterfaceGroup == null) {
            requireInterfaceGroup = new RequirementInterfaceGroup();
            requireInterfaceGroup.setRequirementId(requirementinfo.getId());
            requireInterfaceGroup.setSortGroupTree(manage.getSortGroupTree());
            requireInterfaceGroup.setInterfaceType(manage.getType());
            requireInterfaceGroup.setGroupLastVersion(RequirementInterfaceGroup.J_API_GROUP_VERION_PREFIX + manage.getGroupLastVersion());
            requireInterfaceGroup.setInterfaceId(manage.getId());
            requirementInterfaceGroupService.save(requireInterfaceGroup);
        } else {
            requireInterfaceGroup.setSortGroupTree(manage.getSortGroupTree());

            requireInterfaceGroup.setGroupLastVersion(RequirementInterfaceGroup.J_API_GROUP_VERION_PREFIX + manage.getGroupLastVersion());
            requirementInterfaceGroupService.updateById(requireInterfaceGroup);
        }
    }
    public void syncJapiRequirement(JApiProjectInfo projectInfo,  InterfaceManage manage,boolean forceUpdate) {
        RequirementInfo requirementinfo = requirementInfoService.getByJapiId(manage.getId());
        RequirementInterfaceGroup requireInterfaceGroup = null;
        if (requirementinfo == null) {
            requirementinfo = requirementInfoService.newJapiRequirement(manage.getId(), manage.getName());
            requirementInfoService.save(requirementinfo);


        } else {
            requireInterfaceGroup = requirementInterfaceGroupService.getByRequirementIdAndInterfaceId(requirementinfo.getId(), manage.getId());
        }
        syncRequirementMembers(projectInfo, requirementinfo.getId());
        changeGroupTree(manage.getSortGroupTree(),manage);
        if (requireInterfaceGroup == null) {
            requireInterfaceGroup = new RequirementInterfaceGroup();
            requireInterfaceGroup.setRequirementId(requirementinfo.getId());
            requireInterfaceGroup.setSortGroupTree(manage.getSortGroupTree());
            requireInterfaceGroup.setInterfaceType(manage.getType());
            requireInterfaceGroup.setGroupLastVersion(RequirementInterfaceGroup.J_API_GROUP_VERION_PREFIX + manage.getGroupLastVersion());
            requireInterfaceGroup.setInterfaceId(manage.getId());
            requirementInterfaceGroupService.save(requireInterfaceGroup);
        } else {
            if (requireInterfaceGroup.getGroupLastVersion().startsWith(RequirementInterfaceGroup.J_API_GROUP_VERION_PREFIX)
                    || StringUtils.isEmpty(requireInterfaceGroup.getGroupLastVersion()) || forceUpdate
            ) {
                requireInterfaceGroup.setSortGroupTree(manage.getSortGroupTree());

                requireInterfaceGroup.setGroupLastVersion(RequirementInterfaceGroup.J_API_GROUP_VERION_PREFIX + manage.getGroupLastVersion());
                requirementInterfaceGroupService.updateById(requireInterfaceGroup);
            }
        }

    }

    public void syncJapiInterface(Long id, Integer type, Long interfaceId, boolean forceUpdate) {
        log.info("log.sync_japi_interface: id={},type={},interfaceId={},forceUpdate={}", id, type, interfaceId, forceUpdate);
        List<InterfaceManage> interfaceManages = new ArrayList<>();
        if (interfaceId != null) {
            InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
            interfaceManages.add(interfaceManage);
        } else {
            if (type == GroupTypeEnum.APP.getCode()) {
                final List<InterfaceManage> noAppInterfaces = interfaceManageService.getNoAppInterfaces(InterfaceTypeEnum.HTTP.getCode());
                interfaceManages.addAll(noAppInterfaces);
            } else if (type == GroupTypeEnum.PRD.getCode()) {
                RequirementInfo requirementInfo = requirementInfoService.getById(id);
                Long relatedInterfaceId = requirementInfo.getRelatedId();
                InterfaceManage interfaceManage = interfaceManageService.getById(relatedInterfaceId);
                interfaceManages.add(interfaceManage);
            }

        }
        for (final InterfaceManage manage : interfaceManages) {
            scheduleService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        syncJapiInterface(manage, forceUpdate);
                    } catch (Exception e) {
                        log.error("同步japi接口失败", e);
                    }
                }
            });
        }
    }
        public void syncJapiInterface(Long interfaceId,boolean forceUpdate){
            final InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
            syncJapiInterface(interfaceManage, forceUpdate);
        }
        /**
         * 将japi接口同步到在线联调
         */
        public void syncJapiInterface(InterfaceManage interfaceManage,boolean forceUpdate){
            log.info("japi.begin_sync_japi_interface: interfaceId={},forceUpdate={}", interfaceManage.getId(), forceUpdate);
            UserInfoInSession user = UserSessionLocal.getUser();
            if (user == null) {
                user = new UserInfoInSession();
                user.setUserId("wangjingfang3");
            }
            MetaContextHelper.skipModify(true);
            UserSessionLocal.setUser(user);

            JApiProjectInfo project = japiHttpDataImporter.getProjectInfoIncludePartner(interfaceManage.getRelatedId());
            japiHttpDataImporter.syncInterfaceDesc(interfaceManage, project.getProjectID());
            japiHttpDataImporter.syncInterfaceMembers(project, interfaceManage);
            japiHttpDataImporter.syncInterfaceMethods(project, interfaceManage, forceUpdate);
            syncJapiRequirement(project,  interfaceManage,forceUpdate);
            MetaContextHelper.clearModifyState();

        }
        public List<InterfaceManage> getAllJapiDefaultInterfaces(){
            LambdaQueryWrapper<InterfaceManage> lqw  = new LambdaQueryWrapper<>();
            lqw.eq(InterfaceManage::getYn,1);
            lqw.eq(InterfaceManage::getServiceCode,"japiDefault");
            lqw.select(InterfaceManage::getYn,InterfaceManage::getId,InterfaceManage::getServiceCode,InterfaceManage::getName,InterfaceManage::getRelatedId);

            return interfaceManageService.list(lqw);
        }
        public List<InterfaceManage> getRequirementRelatedInterface(List<RequirementInfo> requirements){
            List<InterfaceManage> interfaces = new ArrayList<>();
            LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
            lqw.eq(InterfaceManage::getYn,1);
            lqw.in(InterfaceManage::getId,requirements.stream().map(RequirementInfo::getRelatedId).collect(Collectors.toList()));
            lqw.select(InterfaceManage::getYn,InterfaceManage::getId,InterfaceManage::getServiceCode,InterfaceManage::getName,InterfaceManage::getRelatedId);

            interfaces = interfaceManageService.list(lqw);
            return interfaces;
        }
        public List<RequirementInfo> getAllFlowRequirementId(){
            LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
            lqw.eq(RequirementInfo::getYn,1);
            lqw.eq(RequirementInfo::getType,2);
            List<RequirementInfo> requirements = requirementInfoService.list(lqw);

            return requirements;
        }
    public Map<Long, List<InterfaceManage>> initAllJapiInterface(){
        final List<JApiProjectInfo> projects = japiHttpDataImporter.getAllProject();
         List<InterfaceManage> japiDefaultInterfaces = getAllJapiDefaultInterfaces();
        japiDefaultInterfaces = japiDefaultInterfaces.stream().filter(i -> i.getRelatedId() != null).collect(Collectors.toList());
        final Map<Long, List<InterfaceManage>> relatedId2Interfaces = japiDefaultInterfaces.stream().collect(Collectors.groupingBy(InterfaceManage::getRelatedId));
        List<InterfaceManage> newInterfaces = new ArrayList<>();
        for (JApiProjectInfo project : projects) {
            final List<InterfaceManage> interfaceManages = relatedId2Interfaces.get(project.getProjectID());
            if(interfaceManages == null){
                log.info("japi.miss_sync_interfaces:projectId={}",project.getProjectID());
                final InterfaceManage interfaceManage = japiHttpDataImporter.newInterfaceManage(project);

                newInterfaces.add(interfaceManage);
                relatedId2Interfaces.put(project.getProjectID(), Arrays.asList(interfaceManage));
            }
        }
        if(!newInterfaces.isEmpty()){
            interfaceManageService.saveBatch(newInterfaces);
        }
        return relatedId2Interfaces;
    }
    public void updateRequirementInfoRelatedId(){
        Map<Long, List<InterfaceManage>> relatedId2Interfaces = initAllJapiInterface();
        List<RequirementInfo> requirements = getAllFlowRequirementId();
        List<InterfaceManage> requirementInterfaces = getRequirementRelatedInterface(requirements);
        final Map<Long, List<InterfaceManage>> id2Interfaces = requirementInterfaces.stream().collect(Collectors.groupingBy(InterfaceManage::getId));

        for (RequirementInfo requirement : requirements) {
            final List<InterfaceManage> interfaceManages = id2Interfaces.get(requirement.getRelatedId());
            if(interfaceManages == null){
                log.error("requirement.miss_sync_interfaces:requirementId={}",requirement.getId());
            }else{
                final List<InterfaceManage> newInterfaces = relatedId2Interfaces.get(interfaceManages.get(0).getRelatedId());
                if(newInterfaces == null){
                    log.error("requirement.mess_related_itnerfaceId:relatedId={}",interfaceManages.get(0).getRelatedId());
                }else{
                    log.info("requirement.update_related：requirementId={},before={},after={}",requirement.getId(),requirement.getRelatedId(),newInterfaces.get(0).getId());
                    requirement.setRelatedId(newInterfaces.get(0).getId());

                    requirementInfoService.updateById(requirement);
                }

            }
        }

    }
    public void clearInvalidInterface() {
            LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
            lqw.isNull(InterfaceManage::getAppId);
            lqw.like(InterfaceManage::getServiceCode, "japi_%");
            lqw.isNull(InterfaceManage::getAppId);
            lqw.eq(InterfaceManage::getYn,1);
            lqw.select(InterfaceManage::getYn,InterfaceManage::getId,InterfaceManage::getServiceCode,InterfaceManage::getName);
            List<InterfaceManage> interfaceManages = interfaceManageService.list(lqw);
            for (InterfaceManage interfaceManage : interfaceManages) {
                if(interfaceManage.getServiceCode().startsWith("japi_")){
                    interfaceManage.setYn(0);
                    interfaceManageService.updateById(interfaceManage);
                }else{
                    log.error("interfaceManage.err_ignore_serviceCode:{}",interfaceManage.getServiceCode());
                }
            }
    }

    public void clearInvalidRequirement() {
         LambdaUpdateWrapper<RequirementInfo> luw = new LambdaUpdateWrapper<>();
         luw.set(RequirementInfo::getYn,0);
         luw.eq(RequirementInfo::getType,2);
         requirementInfoService.update(luw);
    }
}
