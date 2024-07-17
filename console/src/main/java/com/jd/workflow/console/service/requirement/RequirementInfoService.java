package com.jd.workflow.console.service.requirement;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.jd.cjg.flow.sdk.client.WorkFlowClient;
import com.jd.cjg.flow.sdk.model.dto.query.CommonQueryVO;
import com.jd.cjg.flow.sdk.model.dto.query.ProcessDetailQuery;
import com.jd.cjg.flow.sdk.model.dto.submit.CancelProcessInstance;
import com.jd.cjg.flow.sdk.model.dto.submit.ProcessInstance;
import com.jd.cjg.flow.sdk.model.dto.submit.ProcessInstanceVO;
import com.jd.cjg.flow.sdk.model.dto.submit.WorkFlowInstanceVO;
import com.jd.cjg.flow.sdk.model.result.FlowResult;
import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ResourceRoleEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.dao.mapper.requirement.RequirementInfoMapper;
import com.jd.workflow.console.dto.app.CjgFlowCreateResult;
import com.jd.workflow.console.dto.doc.InterfaceCountModel;
import com.jd.workflow.console.dto.doc.InterfaceSortModel;
import com.jd.workflow.console.dto.doc.MethodSortModel;
import com.jd.workflow.console.dto.group.GroupResolveDto;
import com.jd.workflow.console.dto.group.GroupTypeEnum;
import com.jd.workflow.console.dto.group.RequirementTypeEnum;
import com.jd.workflow.console.dto.manage.JsfInterfaceAndMethod;
import com.jd.workflow.console.dto.requirement.FlowInstanceVo;
import com.jd.workflow.console.dto.requirement.InterfaceSpaceDTO;
import com.jd.workflow.console.dto.requirement.RequirementInfoDto;
import com.jd.workflow.console.dto.role.UserRoleDTO;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.dto.requirement.*;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.MethodManage;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.service.*;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.group.impl.RequirementGroupServiceImpl;
import com.jd.workflow.console.service.role.AccRoleServiceAdapter;
import com.jd.workflow.console.service.test.JagileRemoteCaller;
import com.jd.workflow.console.service.test.RequirementWorkflowService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.acl.Owner;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static com.jd.workflow.console.entity.requirement.RequirementInfo.STATUS_PROCESSING;

@Slf4j
@Service
public class RequirementInfoService extends ServiceImpl<RequirementInfoMapper, RequirementInfo> {
    @Autowired
    WorkFlowClient workFlowClient;
    @Autowired
    IMemberRelationService relationService;
    @Autowired
    RequirementWorkflowService workflowService;
    @Autowired
    ScheduledExecutorService defaultScheduledExecutor;
    @Autowired
    IInterfaceManageService interfaceManageService;
    @Autowired
    IAppInfoService appInfoService;

    @Autowired
    IUserInfoService userInfoService;

    @Autowired
    private RequirementInterfaceGroupService requirementInterfaceGroupService;
    @Autowired
    private IMethodManageService methodManageService;

    @Autowired
    RequirementGroupServiceImpl requirementGroupService;
    @Autowired
    JagileRemoteCaller jagileRemoteCaller;
    @Autowired
    InterfaceSpaceService interfaceSpaceService;
    @Autowired
    private AccRoleServiceAdapter accRoleServiceAdapter;


    /**
     * 创建需求
     *
     * @return
     */
    @Transactional
    public CjgFlowCreateResult createRequirement(Long id, ProcessInstance processInstance, List<String> operators) {
        processInstance.setSource(RequirementWorkflowService.source);
        log.info("createRequirement createProcessInstance param:{}", JSON.toJSONString(processInstance));
        final FlowResult<Long> result = workFlowClient.createProcessInstance(processInstance);
        log.info("createRequirement createProcessInstance param:{},response:{}", JSON.toJSONString(processInstance), JSON.toJSONString(result));
        if (!result.checkSuccess()) {
            throw new BizException("创建需求失败：" + result.getMessage());
        }
        CjgFlowCreateResult ret = new CjgFlowCreateResult();
        ret.setFlowId(result.getModel());
        RequirementInfo requirementInfo = new RequirementInfo();
        requirementInfo.setId(id);
        requirementInfo.setRelatedId(result.getModel());
        requirementInfo.setYn(1);
        requirementInfo.setType(RequirementTypeEnum.FLOW.getCode());
        requirementInfo.setName(processInstance.getName());
        requirementInfo.setRelatedRequirementCode(processInstance.getBusinessRequirement());
        requirementInfo.setStatus(STATUS_PROCESSING);
        requirementInfo.setRelatedFlowTemplateId(processInstance.getTemplateCode());
        saveOrUpdate(requirementInfo);
        List<MemberRelation> relations = new ArrayList<>();
        MemberRelation relation = newMemberRelation(UserSessionLocal.getUser().getUserId(), requirementInfo.getId());
        relations.add(relation);
        for (String operator : operators) {
            relation = newMemberRelation(operator, requirementInfo.getId());
            relations.add(relation);
        }
        relationService.saveBatch(relations);
        ret.setId(requirementInfo.getId());
        return ret;
    }

    public void syncStatus(Long flowId) {
        RequirementInfo requirementInfo = getByFlowId(flowId);
        if (requirementInfo == null) {
            return;
        }
        FlowInstanceVo flowInstanceVo = workflowService.flowDetail(flowId);
        requirementInfo.setStatus(flowInstanceVo.getStatus());
        updateById(requirementInfo);
    }

    private InterfaceManage queryJsfInterface(JsfInterfaceAndMethod jsfInterfaceAndMethod) {
        LambdaQueryWrapper<InterfaceManage> lqw = new LambdaQueryWrapper<>();
        lqw.eq(InterfaceManage::getYn, 1);
        lqw.eq(InterfaceManage::getServiceCode, jsfInterfaceAndMethod.getJsfName());
        lqw.and(childWrapper -> {
            childWrapper.inSql(InterfaceManage::getAppId, "select app_id from app_info_members where erp='" + UserSessionLocal.getUser().getUserId() + "'")
                    .or().inSql(InterfaceManage::getId, "select resource_id from member_relation where yn = 1 and resource_type=1 and user_code='" + UserSessionLocal.getUser().getUserId() + "'");
        });

        List<InterfaceManage> interfaceManages = interfaceManageService.list(lqw);
        if (interfaceManages.isEmpty()) return null;
        return interfaceManages.get(0);
    }

    public List<InterfaceSortModel> queryJsfInterfaces(List<JsfInterfaceAndMethod> interfaceAndMethods) {
        List<InterfaceSortModel> list = new ArrayList<>();
        for (JsfInterfaceAndMethod interfaceAndMethod : interfaceAndMethods) {
            InterfaceManage interfaceManage = queryJsfInterface(interfaceAndMethod);
            if (interfaceManage == null) continue;
            InterfaceSortModel sortModel = new InterfaceSortModel();
            sortModel.setId(interfaceManage.getId());
            sortModel.setEnName(interfaceManage.getServiceCode());

            if (interfaceManage.getAppId() != null) {
                AppInfo app = appInfoService.getById(interfaceManage.getAppId());
                sortModel.setAppName(app.getAppName());
            }
            sortModel.setName(interfaceManage.getName());
            {
                LambdaQueryWrapper<MethodManage> methodLqw = new LambdaQueryWrapper<>();
                methodLqw.eq(MethodManage::getYn, 1);
                methodLqw.in(MethodManage::getMethodCode, interfaceAndMethod.getMethodNames());
                methodLqw.in(MethodManage::getInterfaceId, interfaceManage.getId());
                List<MethodManage> methods = methodManageService.list(methodLqw);

                sortModel.getChildren().addAll(methods.stream().map(item -> {
                    MethodSortModel methodSortModel = new MethodSortModel();
                    methodSortModel.setId(item.getId());
                    methodSortModel.setName(item.getName());
                    methodSortModel.setEnName(item.getMethodCode());
                    return methodSortModel;
                }).collect(Collectors.toList()));
            }
            list.add(sortModel);
        }
        return list;
    }

    public RequirementInfo newJapiRequirement(Long related, String name) {
        RequirementInfo requirementInfo = new RequirementInfo();
        requirementInfo.setRelatedId(related);
        requirementInfo.setYn(1);
        requirementInfo.setType(RequirementTypeEnum.JAPI.getCode());
        requirementInfo.setName(name);
        requirementInfo.setCreator("syncRequirement");
        requirementInfo.setCreated(new Date());
        return requirementInfo;
    }

    public RequirementInfo getByJapiId(Long relatedId) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getRelatedId, relatedId);
        lqw.eq(RequirementInfo::getType, RequirementTypeEnum.JAPI.getCode());
        lqw.eq(RequirementInfo::getYn, 1);
        return getOne(lqw);
    }

    public RequirementInfo getFlowRequirementId(Long id) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getId, id);
        lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
        lqw.eq(RequirementInfo::getYn, 1);
        return getOne(lqw);
    }

    public RequirementInfo getRequirementByFlowRelatedId(Long id) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getRelatedId, id);
        lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
        lqw.eq(RequirementInfo::getYn, 1);
        return getOne(lqw);
    }

    public void mody(Long id, String name, Integer type, Integer status, Integer newType) {
        if (type == 2) {
            LambdaUpdateWrapper<RequirementInfo> lqw = new LambdaUpdateWrapper<>();
            lqw.eq(RequirementInfo::getType, RequirementTypeEnum.JAPI.getCode());
            lqw.eq(null != id, RequirementInfo::getId, id);
            lqw.eq(RequirementInfo::getYn, 1);
            lqw.set(null != status, RequirementInfo::getStatus, status);
            lqw.set(RequirementInfo::getName, name);
            lqw.set(null != newType, RequirementInfo::getType, newType);
            update(lqw);
        }
        if (type == 1) {
            LambdaUpdateWrapper<RequirementInfo> lqw2 = new LambdaUpdateWrapper<>();
            lqw2.eq(null != id, RequirementInfo::getId, id);
            lqw2.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
            lqw2.set(null != status, RequirementInfo::getStatus, status);
            lqw2.set(RequirementInfo::getName, name);
            lqw2.set(null != newType, RequirementInfo::getType, newType);
            update(lqw2);
        }
    }

    public Integer getRequirementMethodCount(Long requirementId) {
        GroupResolveDto dto = new GroupResolveDto();
        dto.setId(requirementId);
        dto.setType(GroupTypeEnum.PRD.getCode());
        List<InterfaceCountModel> rootGroups = requirementGroupService.getRootGroups(dto, null);
        return rootGroups.stream().mapToInt(item -> item.getCount()).sum();
    }

    public MemberRelation newMemberRelation(String userCode, Long resourceId) {
        MemberRelation relation = new MemberRelation();
        relation.setUserCode(userCode);
        relation.setResourceType(ResourceTypeEnum.PRD_GROUP.getCode());
        relation.setResourceId(resourceId);
        relation.setResourceRole(ResourceRoleEnum.MEMBER.getCode());
        relation.setYn(1);
        relation.setCreator("createRequirement");
        relation.setCreated(new Date());
        return relation;
    }


    public IPage<RequirementInfoDto> pageList(Long current, Long size, String name, Integer status, String sortBy, String departmentId) {

        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(name)) {
            lqw.nested(child -> child.like(RequirementInfo::getName, name)
                    .or().like(RequirementInfo::getRelatedRequirementCode, name)
                    .or().like(RequirementInfo::getSpaceName, name));
        }
        lqw.eq(RequirementInfo::getYn, 1);
        lqw.like(StringUtils.isNotBlank(departmentId), RequirementInfo::getDepartmentIds, departmentId);
        //未启用--模版为空
        if (status != null && status == 3) {
            lqw.isNull(RequirementInfo::getRelatedFlowTemplateId);
        } else {
            if (status != null) {
                lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
                if (status == 1) {
                    lqw.isNotNull(RequirementInfo::getRelatedFlowTemplateId);
                    lqw.in(RequirementInfo::getStatus, Arrays.asList(0, 1));
                } else {
                    lqw.eq(status != null, RequirementInfo::getStatus, status);
                }
            }
        }
        if ("create_at".equals(sortBy) || StringUtils.isEmpty(sortBy)) {
            lqw.orderByDesc(StringUtils.isNotBlank(sortBy), RequirementInfo::getCreated);
        } else if ("modify_at".equals(sortBy)) {
            lqw.orderByDesc(StringUtils.isNotBlank(sortBy), RequirementInfo::getModified);
        }

        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());
        boolean myAdmin = false;

        if (userRoleDTO.getJapiAdmin()) {
            //管理员可以看到所有数据。
            myAdmin = true;
        } else if (userRoleDTO.getDeptLeader()) {
            //部门负责人可以看到所属部门所有数据
            myAdmin = true;
            lqw.inSql(RequirementInfo::getId, "select mr.resource_id from member_relation mr  JOIN user_info ui ON mr.user_code = ui.user_code  WHERE mr.yn=1 and mr.resource_type=20 and ui.dept LIKE '" + userRoleDTO.getDept() + "%'");
        } else if (userRoleDTO.getJapiDepartment()) {
            //部门接口人，可以看到所属部门数据
            myAdmin = true;
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
            lqw.inSql(RequirementInfo::getId, "select mr.resource_id from member_relation mr  JOIN user_info ui ON mr.user_code = ui.user_code  WHERE mr.yn=1 and mr.resource_type=20 and ui.dept LIKE '" + deptBuilder.toString() + "%'");

        } else {
            //普通用户，只能看到自己是成员的数据

            lqw.inSql(RequirementInfo::getId, "select resource_id from member_relation where resource_type=20 and yn = 1 and user_code='" + UserSessionLocal.getUser().getUserId() + "'");
        }
        boolean isAdmin = myAdmin;

//        boolean isAdmin = relationService.checkTenantAdmin(UserSessionLocal.getUser().getUserId());
//        //按照部门过滤，不通过erp过滤
//        if (StringUtils.isEmpty(departmentId) && !isAdmin) {
//            lqw.inSql(RequirementInfo::getId, "select resource_id from member_relation where resource_type=20 and yn = 1 and user_code='" + UserSessionLocal.getUser().getUserId() + "'");
//        }

        log.info("测试sql-->{}", lqw.getTargetSql());
        IPage<RequirementInfo> page = page(new Page<>(current, size), lqw);
        List<Long> flowIds = page.getRecords().stream().filter(item -> {
            return item.getType().equals(RequirementTypeEnum.FLOW.getCode());
        }).map(item -> item.getRelatedId()).collect(Collectors.toList());
        IPage<RequirementInfoDto> result = page.convert(item -> {
            //if(item.getType().equals(RequirementTypeEnum.JAPI.getCode())){
            RequirementInfoDto dto = new RequirementInfoDto();
            BeanUtils.copyProperties(item, dto);
            dto.setCode(item.getRelatedRequirementCode());
            dto.setName(item.getName());
            dto.setCreateAt(item.getCreated());
            dto.setCreateBy(item.getCreator());
            dto.setTemplateCode(item.getRelatedFlowTemplateId());
            dto.setStatus(item.getStatus());
            if (dto.getNodeListAtMoment() == null) {
                dto.setNodeListAtMoment(new HashSet<>());
            }
            dto.setGitBranch(item.getGitBranch());
            return dto;
        });
        if (!flowIds.isEmpty()) {
            CommonQueryVO vo = new CommonQueryVO();
            vo.setSource(RequirementWorkflowService.source);
            vo.setFlowIdList(flowIds);
            final FlowResult<List<WorkFlowInstanceVO>> pageListResult = workFlowClient.getFlowInstanceList(vo);
            if (!pageListResult.checkSuccess()) {
                throw new BizException("查询需求列表失败：" + pageListResult.getMessage());
            }
            final List<WorkFlowInstanceVO> flowInstanceVOS = pageListResult.getModel();
            Map<Long, List<WorkFlowInstanceVO>> id2Defintions = flowInstanceVOS.stream().collect(Collectors.groupingBy(item -> item.getFlowId()));
            result.getRecords().forEach(item -> {
                if (item.getType().equals(RequirementTypeEnum.FLOW.getCode())) {
                    final List<WorkFlowInstanceVO> workFlowInstanceVOS = id2Defintions.get(item.getRelatedId());
                    if (workFlowInstanceVOS != null && !workFlowInstanceVOS.isEmpty()) {
                        BeanUtils.copyProperties(workFlowInstanceVOS.get(0), item);
                        if (item.getNodeListAtMoment() == null) {
                            item.setNodeListAtMoment(new HashSet<>());
                        }
                    }
                }
            });
        }
        //接口空间数据处理
        result.getRecords().forEach(item -> {
            if (item.getType().equals(RequirementTypeEnum.JAPI.getCode())) {
                item.setSpaceName(item.getName());
                item.setName("");
                if (StringUtils.isEmpty(item.getTemplateCode())) {
                    item.setStatus(3);
                }
            }
        });
        Set<String> userCodes = result.getRecords().stream().map(item -> item.getCreateBy()).collect(Collectors.toSet());
        List<Future> list = new ArrayList<>();
        for (String userCode : userCodes) {
            if (userCode == null) continue;
            final Future<?> future = defaultScheduledExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        final UserInfo user = userInfoService.getUser(userCode);
                        if (user == null) return;
                        result.getRecords().forEach(item -> {
                            if (item.getCreateBy() == null) return;
                            if (item.getCreateBy().equals(userCode)) {
                                RequirementInfoDto.Creator creator = new RequirementInfoDto.Creator();
                                creator.setHeadImg(user.getHeadImg());
                                creator.setUserName(userCode);
                                creator.setRealName(user.getUserName());
                                item.setCreator(creator);
                            }
                        });
                    } catch (Exception e) {
                        log.error("查询用户信息失败", e);
                    }
                }
            });
            list.add(future);
        }
        String pin = UserSessionLocal.getUser().getUserId();
        //空间管理人
        for (RequirementInfoDto record : result.getRecords()) {
            final Future<?> future = defaultScheduledExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    fillIsOwner(record.getId(), record, isAdmin, pin);
                }
            });
            list.add(future);
        }
        for (Future future : list) {
            try {
                future.get();
            } catch (Exception e) {
                log.error("user.err_get_user", e);
            }
        }
        return result;

    }

    private void fillIsOwner(Long spaceId, RequirementInfoDto dto, Boolean isAdmin, String pin) {
        InterfaceSpaceUser users = interfaceSpaceService.getSpaceUser(spaceId);
        dto.setMembers(users.getMembers());
        dto.setOwner(users.getOwner());
        if (CollectionUtils.isNotEmpty(users.getMembers())) {
            List<String> members = users.getMembers().stream().map(UserInfoDTO::getErp).collect(Collectors.toList());
            dto.setIsMember(members.contains(pin));
        }

        if (isAdmin) {
            dto.setIsOwner(true);
        } else {
            if (Objects.isNull(users.getOwner())) {
                dto.setIsOwner(false);
            } else {
                dto.setIsOwner(pin.equals(users.getOwner().getErp()));
            }
        }
    }


    public RequirementInfo getByFlowId(Long relatedId) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getRelatedId, relatedId);
        lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
        lqw.eq(RequirementInfo::getYn, 1);
        return getOne(lqw);
    }

    public List<MemberRelation> getMembers(Long id) {
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MemberRelation::getResourceId, id);
        lqw.eq(MemberRelation::getResourceType, ResourceTypeEnum.PRD_GROUP.getCode());
        lqw.eq(MemberRelation::getYn, 1);
        return relationService.list(lqw);
    }

    public void syncMember(Long flowId) {
        final RequirementInfo flow = getByFlowId(flowId);
        if (flow == null) return;
        Set<String> members = workflowService.getPersonsOfFlow(flowId);
        final List<MemberRelation> exist = getMembers(flow.getId());
        final List<String> existMembers = exist.stream().map(item -> item.getUserCode()).collect(Collectors.toList());
        final List<String> addMembers = members.stream().filter(item -> !existMembers.contains(item)).collect(Collectors.toList());
        List<MemberRelation> saved = new ArrayList<>();
        for (String addMember : addMembers) {

            saved.add(newMemberRelation(addMember, flow.getId()));
        }
        relationService.saveBatch(saved);
    }

    /**
     * 移除需求
     *
     * @return
     */
    public Boolean remove(Long id) {
        Guard.notEmpty(id, "id不允许为空");
        final RequirementInfo entity = getById(id);
        Guard.notEmpty(entity, "需求不存在");
        if (entity.getType().equals(RequirementTypeEnum.FLOW.getCode())) {
            CancelProcessInstance p = new CancelProcessInstance();
            p.setSource(RequirementWorkflowService.source);
            p.setFlowId(entity.getRelatedId());
            p.setExecutor(UserSessionLocal.getUser().getUserId());
            FlowResult<Boolean> result = workFlowClient.cancelProcessInstance(p);
            if (!result.checkSuccess()) {
                throw new BizException("移除需求失败：" + result.getMessage());
            }
        }


        entity.setYn(0);
        updateById(entity);
        return true;
    }

    public RequirementInfoDto getEntityById(Long id) {
        final RequirementInfo entity = getById(id);
        Guard.notEmpty(entity, "需求不存在");
        RequirementInfoDto dto = new RequirementInfoDto();
        BeanUtils.copyProperties(entity, dto);
        dto.setCreateBy(entity.getCreator());
        dto.setCreateAt(entity.getCreated());
        dto.setCode(entity.getRelatedRequirementCode());
        dto.setTemplateCode(entity.getRelatedFlowTemplateId());
        if (entity.getType().equals(RequirementTypeEnum.FLOW.getCode())) {
            dto.setFlowId(entity.getRelatedId());
            dto.setRelatedId(entity.getRelatedId());
        }
        boolean isAdmin = relationService.checkTenantAdmin(UserSessionLocal.getUser().getUserId());
        try {
            // 判断是否是japi_admin
            UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());
            isAdmin = isAdmin || userRoleDTO.getJapiAdmin();
        } catch (Exception e) {
            log.error("getEntityById.queryUserRole error", e);
        }
        fillIsOwner(id, dto, isAdmin, UserSessionLocal.getUser().getUserId());
        return dto;
    }


    public FlowInstanceVo convert(RequirementInfo requirementInfo) {
        FlowInstanceVo flowInstanceVo = new FlowInstanceVo();
        if (requirementInfo.getType() == 1) {
            flowInstanceVo.setFlowId(requirementInfo.getRelatedId());
        }
        flowInstanceVo.setName(StringUtils.isNotBlank(requirementInfo.getSpaceName()) ? requirementInfo.getSpaceName() : requirementInfo.getName());
        flowInstanceVo.setId(requirementInfo.getId());
        flowInstanceVo.setTemplateCode(requirementInfo.getRelatedFlowTemplateId());
        flowInstanceVo.setStatus(requirementInfo.getStatus());
        flowInstanceVo.setCode(requirementInfo.getRelatedRequirementCode());
        return flowInstanceVo;
    }

    public FlowInstanceVo flowDetail(Long id) {
        final RequirementInfo requirementInfo = getById(id);
        return convert(requirementInfo);
    }

    public IPage<FlowInstanceVo> queryLocalRequirementList(String name, Integer type, Integer current, Integer pageSize) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getYn, 1);
        lqw.eq(type != null, RequirementInfo::getType, type);
        if (StringUtils.isNotBlank(name)) {
            lqw.like(RequirementInfo::getName, name);
            lqw.or().like(RequirementInfo::getSpaceName, name);
        }
        lqw.inSql(RequirementInfo::getId, "select resource_id from member_relation where resource_type=20 and yn = 1 and user_code='" + UserSessionLocal.getUser().getUserId() + "'");
        final Page<RequirementInfo> page = page(new Page<>(current, pageSize), lqw);
        return page.convert(item -> {
            return convert(item);

        });
    }


    public boolean createSpace(InterfaceSpaceDTO interfaceSpaceDTO) {


        RequirementInfo requirementInfo = new RequirementInfo();

        requirementInfo.setYn(1);
        requirementInfo.setType(RequirementTypeEnum.JAPI.getCode());
        requirementInfo.setName(interfaceSpaceDTO.getName());
        requirementInfo.setDescription(interfaceSpaceDTO.getDesc());
        requirementInfo.setStatus(STATUS_PROCESSING);
        save(requirementInfo);
        List<MemberRelation> relations = new ArrayList<>();
        if (StringUtils.isEmpty(interfaceSpaceDTO.getOwner())) {
            MemberRelation relation = newMemberRelation(UserSessionLocal.getUser().getUserId(), requirementInfo.getId());
            relation.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
            relations.add(relation);
        } else {
            MemberRelation relation = newMemberRelation(interfaceSpaceDTO.getOwner(), requirementInfo.getId());
            relation.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
            relations.add(relation);
        }

        for (String operator : interfaceSpaceDTO.getMembers()) {
            MemberRelation relation = newMemberRelation(operator, requirementInfo.getId());
            relations.add(relation);
        }
        return relationService.saveBatch(relations);

    }

    public void updateRequirementMethodStatus(RequirementInfo requirementInfo) {
        log.info("updateRequirementMethodStatus id:{}", requirementInfo.getId());
        LambdaQueryWrapper<RequirementInterfaceGroup> lqwGroup = new LambdaQueryWrapper<>();
        List<Integer> interfaceTypeList = Lists.newArrayList();
        interfaceTypeList.add(InterfaceTypeEnum.HTTP.getCode());
        interfaceTypeList.add(InterfaceTypeEnum.JSF.getCode());
        lqwGroup.eq(RequirementInterfaceGroup::getRequirementId, requirementInfo.getId()).in(RequirementInterfaceGroup::getInterfaceType, interfaceTypeList);
        lqwGroup.select(RequirementInterfaceGroup::getInterfaceId);


        List<RequirementInterfaceGroup> httpRequirementInterfaceGroupList = requirementInterfaceGroupService.list(lqwGroup);
        if (CollectionUtils.isEmpty(httpRequirementInterfaceGroupList)) {
            return;
        }
        List<Long> httpInterfaceIdList = httpRequirementInterfaceGroupList.stream().map(RequirementInterfaceGroup::getInterfaceId).collect(Collectors.toList());

        LambdaQueryWrapper<MethodManage> lqwMethod = new LambdaQueryWrapper<>();
        lqwMethod.eq(BaseEntity::getYn, 1);
        lqwMethod.in(MethodManage::getInterfaceId, httpInterfaceIdList);
        List<MethodManage> methodManageList = methodManageService.list(lqwMethod);
        if (CollectionUtils.isEmpty(methodManageList)) {
            return;
        }
        //接口设置成完成
        for (MethodManage methodManage : methodManageList) {
            methodManageService.updateStatus(methodManage.getId(), 1);
        }

    }

    public List<RequirementStatisticDto> exportRequirementData() {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getYn, 1);
        lqw.eq(RequirementInfo::getType, 1); // 工作流
        List<RequirementInfo> requirementInfos = list(lqw);
        return requirementInfos.stream().map(item -> requirementToDto(item)).collect(Collectors.toList());
    }

    public RequirementStatisticDto requirementToDto(RequirementInfo requirementInfo) {
        RequirementStatisticDto dto = new RequirementStatisticDto();
        BeanUtils.copyProperties(requirementInfo, dto);
        dto.setStatus(requirementInfo.getStatus());
        List<MemberRelation> members = getMembers(requirementInfo.getId());
        String memberStr = members.stream().map(item -> item.getUserCode()).collect(Collectors.joining(","));
        dto.setMembers(memberStr);

        if (requirementInfo.getRelatedId() != null) {
            ProcessDetailQuery query = new ProcessDetailQuery();
            query.setSource(RequirementWorkflowService.source);
            query.setExecutor(requirementInfo.getCreator());
            query.setFlowId(requirementInfo.getRelatedId());
            FlowResult<ProcessInstanceVO> detail = workFlowClient.detail(query);
            if (detail.getModel() != null) {
                dto.setTemplateCode(detail.getModel().getTemplateCode());
            }
        }

        return dto;
    }
}
