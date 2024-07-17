package com.jd.workflow.console.service.requirement.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jd.cjg.flow.sdk.client.WorkFlowClient;
import com.jd.cjg.flow.sdk.model.dto.query.CommonQueryVO;
import com.jd.cjg.flow.sdk.model.dto.submit.WorkFlowInstanceVO;
import com.jd.cjg.flow.sdk.model.result.FlowResult;
import com.jd.common.util.StringUtils;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.InterfaceTypeEnum;
import com.jd.workflow.console.base.enums.ResourceRoleEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.dao.mapper.requirement.RequirementInfoMapper;
import com.jd.workflow.console.dto.group.RequirementTypeEnum;
import com.jd.workflow.console.dto.requirement.*;
import com.jd.workflow.console.dto.role.UserRoleDTO;
import com.jd.workflow.console.dto.test.jagile.DemandDetail;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.env.EnvConfig;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.entity.requirement.RequirementInfoLog;
import com.jd.workflow.console.entity.requirement.RequirementInterfaceGroup;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.console.service.IInterfaceManageService;
import com.jd.workflow.console.service.env.IEnvConfigService;
import com.jd.workflow.console.service.group.RequirementInterfaceGroupService;
import com.jd.workflow.console.service.impl.MemberRelationServiceImpl;
import com.jd.workflow.console.service.requirement.InterfaceSpaceService;
import com.jd.workflow.console.service.requirement.RequirementInfoLogService;
import com.jd.workflow.console.service.role.AccRoleServiceAdapter;
import com.jd.workflow.console.service.test.JagileRemoteCaller;
import com.jd.workflow.console.service.test.RequirementWorkflowService;
import com.jd.workflow.metrics.client.DemandUserResponse;
import com.jd.workflow.metrics.client.UserResponse;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.jd.workflow.console.entity.requirement.RequirementInfo.STATUS_NO;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/29
 */
@Slf4j
@Service
public class InterfaceSpaceServiceImpl extends ServiceImpl<RequirementInfoMapper, RequirementInfo> implements InterfaceSpaceService {

    @Autowired
    private MemberRelationServiceImpl relationService;

    @Autowired
    private RequirementInfoLogService requirementInfoLogService;

    @Autowired
    private RequirementInterfaceGroupService requirementInterfaceGroupService;


    @Autowired
    private UserHelper userHelper;

    @Autowired
    private IEnvConfigService envConfigService;

    @Autowired
    JagileRemoteCaller jagileRemoteCaller;

    @Autowired
    WorkFlowClient workFlowClient;

    @Autowired
    private AccRoleServiceAdapter accRoleServiceAdapter;

    @Autowired
    IInterfaceManageService interfaceManageService;

    /**
     * 创建空间
     *
     * @param interfaceSpaceDTO
     * @return
     */
    @Transactional
    public Long createSpace(InterfaceSpaceDTO interfaceSpaceDTO) {

        if (StringUtils.isEmpty(interfaceSpaceDTO.getOwner())) {
            interfaceSpaceDTO.setOwner(UserSessionLocal.getUser().getUserId());
        }
        RequirementInfo requirementInfo = new RequirementInfo();
        requirementInfo.setYn(1);
        requirementInfo.setType(RequirementTypeEnum.FLOW.getCode());
        requirementInfo.setName(interfaceSpaceDTO.getName());
        requirementInfo.setSpaceName(interfaceSpaceDTO.getSpaceName());
        requirementInfo.setDescription(interfaceSpaceDTO.getDesc());
        requirementInfo.setRelatedRequirementCode(interfaceSpaceDTO.getCode());
        requirementInfo.setStatus(STATUS_NO);
        requirementInfo.setCreator(UserSessionLocal.getUser().getUserId());
        requirementInfo.setCreated(new Date());
        if (StringUtils.isEmpty(interfaceSpaceDTO.getSpaceName())) {
            requirementInfo.setSpaceName(interfaceSpaceDTO.getName());
        } else {
            requirementInfo.setSpaceName(interfaceSpaceDTO.getSpaceName());
        }
        String demartMentIds = getDemartMentIds(interfaceSpaceDTO.getCode(),interfaceSpaceDTO.getOwner());
        requirementInfo.setDepartmentIds(demartMentIds);
        //保存空间
        save(requirementInfo);

        List<MemberRelation> relations = new ArrayList<>();

        MemberRelation relation = newMemberRelation(interfaceSpaceDTO.getOwner(), requirementInfo.getId());
        relation.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
        relations.add(relation);
        if (CollectionUtils.isNotEmpty(interfaceSpaceDTO.getMembers())) {
            for (String operator : interfaceSpaceDTO.getMembers()) {
                if (operator.equals(interfaceSpaceDTO.getOwner())) {
                    //owner 不在成员列表
                    continue;
                }
                relation = newMemberRelation(operator, requirementInfo.getId());
                relations.add(relation);
            }
        }
        //添加成员
        relationService.saveBatch(relations);

        //添加日志
        requirementInfoLogService.createLog(requirementInfo.getId(), UserSessionLocal.getUser().getUserId(), new Date(), "创建了空间");

        return requirementInfo.getId();


    }

    /**
     * 获取需求部门
     * 提交人、受理人、关注人，所在的部门
     *
     * @param demandCode
     * @return
     */
    private String getDemartMentIds(String demandCode,String erp) {
        CommonResult<DemandDetail> detail = jagileRemoteCaller.getDemandByCode(demandCode,erp);
        if (null == detail.getData()) {
            return null;
        }
        Set<String> orgIds = new HashSet<>();
        DemandUserResponse relatedUsers = detail.getData().getRelatedUsers();
        if (null != relatedUsers) {
            if (CollectionUtils.isNotEmpty(relatedUsers.getConserners())) {
                //关注人部门
                List<String> consernersOrgIds = relatedUsers.getConserners().stream().map(UserResponse::getOrgId).collect(Collectors.toList());
                orgIds.addAll(getThirdOrgId(consernersOrgIds));
            }
            if (null != relatedUsers.getRecipient()) {
                //受理人部门
                String recipientOrgId = relatedUsers.getRecipient().getOrgId();
                orgIds.add(recipientOrgId);
            }
        }
        //提交人部门
        String ordId = detail.getData().getProposer().getOrgId();
        orgIds.add(ordId);
        return org.apache.commons.lang.StringUtils.join(orgIds, ",");
    }


    private List<String> getThirdOrgId(List<String> orgPaths) {
        List<String> data = new ArrayList<String>();
        if (CollectionUtils.isNotEmpty(orgPaths)) {
            for (String orgPath : orgPaths) {
                data.add(orgPath);
            }
        }
        return data;
    }


    @Override
    public void addOrgIds(Long id) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getYn, 1);
        lqw.eq(null != id, RequirementInfo::getId, id);
        lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
        List<RequirementInfo> data = list(lqw);

        //获取模版id
        CommonQueryVO vo = new CommonQueryVO();
        vo.setSource(RequirementWorkflowService.source);
        vo.setFlowIdList(data.stream().map(RequirementInfo::getRelatedId).collect(Collectors.toList()));
        FlowResult<List<WorkFlowInstanceVO>> pageListResult = workFlowClient.getFlowInstanceList(vo);
        Map<Long, List<WorkFlowInstanceVO>> id2Defintions = null;
        if (!pageListResult.checkSuccess()) {
            log.error("查询需求列表失败：" + pageListResult.getMessage());
        } else {
            final List<WorkFlowInstanceVO> flowInstanceVOS = pageListResult.getModel();
            id2Defintions = flowInstanceVOS.stream().collect(Collectors.groupingBy(item -> item.getFlowId()));
        }

        for (RequirementInfo s : data) {
            //获取部门id信息
            String ss = getDemartMentIds(s.getRelatedRequirementCode(),s.getCreator());
            if (null != id2Defintions && CollectionUtils.isNotEmpty(id2Defintions.get(s.getRelatedId()))) {
                s.setRelatedFlowTemplateId(id2Defintions.get(s.getRelatedId()).get(0).getTemplateCode());
            }
            s.setDepartmentIds(ss);
            updateById(s);
        }

    }

    public void updateTemplate(Long id) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getYn, 1);
        lqw.eq(null != id, RequirementInfo::getId, id);
        lqw.isNull(RequirementInfo::getRelatedFlowTemplateId);
        List<RequirementInfo> data = list(lqw);
        for (RequirementInfo datum : data) {
            CommonQueryVO vo = new CommonQueryVO();
            vo.setSource(RequirementWorkflowService.source);
            List<Long> flowIdList = new ArrayList<Long>();
            flowIdList.add(datum.getRelatedId());
            vo.setFlowIdList(flowIdList);
            FlowResult<List<WorkFlowInstanceVO>> pageListResult = workFlowClient.getFlowInstanceList(vo);
            if (null != pageListResult && CollectionUtils.isNotEmpty(pageListResult.getModel()) && StringUtils.isNotEmpty(pageListResult.getModel().get(0).getTemplateCode())) {
//                info.setId(datum.getId());
                datum.setStatus(pageListResult.getModel().get(0).getStatus());
                datum.setRelatedFlowTemplateId(pageListResult.getModel().get(0).getTemplateCode());
                updateById(datum);
            }
        }
    }


    public void updateMember(Long id) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getYn, 1);
        lqw.eq(null != id, RequirementInfo::getId, id);
        lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
        lqw.notIn(RequirementInfo::getId, "select resource_id from member_relation where resource_type=20 and yn = 1 and resource_role=2");
        List<RequirementInfo> data = list(lqw);
        for (RequirementInfo datum : data) {
            MemberRelation relation = newMemberRelation(datum.getCreator(), datum.getId());
            relation.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
            Collection<MemberRelation> relations = new ArrayList<>();
            relations.add(relation);
            //添加成员
            relationService.saveBatch(relations);
        }
    }

    private MemberRelation newMemberRelation(String userCode, Long resourceId) {
        MemberRelation relation = new MemberRelation();
        relation.setUserCode(userCode);
        relation.setResourceType(ResourceTypeEnum.PRD_GROUP.getCode());
        relation.setResourceId(resourceId);
        relation.setResourceRole(ResourceRoleEnum.MEMBER.getCode());
        relation.setYn(1);
        return relation;
    }

    public Boolean openSpace(InterfaceSpaceDTO spaceDTO) {
        RequirementInfo requirementInfo = new RequirementInfo();
        requirementInfo.setId(spaceDTO.getId());
        requirementInfo.setOpenType(spaceDTO.getOpenType());
        requirementInfo.setOpensSolutionName(spaceDTO.getOpenSolutionName());
        requirementInfo.setOpenDesc(spaceDTO.getOpenDesc());
        requirementInfo.setShelves(spaceDTO.getShelve());
        return updateById(requirementInfo);
    }

    @Override
    public IPage<RequirementInfoDto> queryOpenSpaceList(InterfaceSpaceParam param) {

        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BaseEntity::getYn, 1);
//        lqw.eq(RequirementInfo::getShelves,1);
        lqw.isNotNull(RequirementInfo::getRelatedRequirementCode);
        //requirement_interface_group
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(param.getOpenSolutionSpaceName())) {
            lqw.like(RequirementInfo::getName, param.getOpenSolutionSpaceName());
        }
        lqw.like(StringUtils.isNotBlank(param.getDepartmentId()), RequirementInfo::getDepartmentIds, param.getDepartmentId());
        if (StringUtils.isNotEmpty(param.getAdmin())) {
            //使用校验方法校验用户输入的合法性
            if (sqlValidate(param.getAdmin())) {
            }
            lqw.inSql(RequirementInfo::getId, "select resource_id from member_relation where resource_type=20 and resource_role=2 and yn = 1 and user_code='" + param.getAdmin() + "'");
        }
        if (null != param.getInterfaceType()) {
            if (sqlValidate(param.getInterfaceType() + "")) {
            }
            lqw.inSql(RequirementInfo::getId, "select requirement_id from requirement_interface_group where interface_type='" + param.getInterfaceType() + "'");
        }

        Page page = new Page<>(param.getCurrent(), param.getSize());
        Page<RequirementInfo> requirementInfoPage = page(page, lqw);
        IPage<RequirementInfoDto> result = requirementInfoPage.convert(item -> {
            RequirementInfoDto dto = new RequirementInfoDto();
            BeanUtils.copyProperties(item, dto);
//            dto.setOpenSolutionName(item.getOpensSolutionName());
            dto.setCode(item.getRelatedRequirementCode());
            dto.setName(item.getName());
            dto.setCreateAt(item.getCreated());
            dto.setCreateBy(item.getCreator());
            dto.setTemplateCode(item.getRelatedFlowTemplateId());
            dto.setStatus(item.getStatus());
            if (dto.getNodeListAtMoment() == null) {
                dto.setNodeListAtMoment(new HashSet<>());
            }
            InterfaceSpaceUser users = getSpaceUser(item.getId());
            dto.setMembers(users.getMembers());
            dto.setOwner(users.getOwner());
            dto.setMethodTypes(getMethodTypes(item.getId()));
            //空间描述暂时没了
            dto.setOpenDesc("");
            return dto;
        });
        return result;
    }

    private List<Integer> getMethodTypes(Long spaceId) {
        List<Integer> types = new ArrayList<>();
        LambdaQueryWrapper<RequirementInterfaceGroup> lqwGroup = new LambdaQueryWrapper<>();
        lqwGroup.select(RequirementInterfaceGroup::getInterfaceType);
        lqwGroup.eq(RequirementInterfaceGroup::getRequirementId, spaceId);
        List<RequirementInterfaceGroup> groups = requirementInterfaceGroupService.list(lqwGroup);
        Map<Integer, List<RequirementInterfaceGroup>> res = groups.stream().collect(Collectors.groupingBy(RequirementInterfaceGroup::getInterfaceType));
        types.addAll(res.keySet());

        return types;
    }


    protected static boolean sqlValidate(String str) {
        str = str.toLowerCase();//统一转为小写
        //危险字符
        String badStr = "'|\"|and|exec|execute|insert|create|drop|table|from|grant|use|group_concat|column_name|" +
                "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +
                "chr|mid|master|case|truncate|char|declare|or|xor|&|;|-|--|+|,|like|//|/|%|#";//过滤掉的sql关键字，可以手动添加
        String[] badStrs = badStr.split("\\|");
        for (int i = 0; i < badStrs.length; i++) {
            if (str.indexOf(badStrs[i]) != -1) {
                return true;
            }
        }
        return false;
    }


    @Transactional
    public Long editSpace(InterfaceSpaceDTO interfaceSpaceDTO) {

        List<MemberRelation> memberRelationList = getMembers(interfaceSpaceDTO.getId());
        MemberRelation owner = null;
        for (MemberRelation memberRelation : memberRelationList) {
            if (ResourceRoleEnum.ADMIN.getCode().equals(memberRelation.getResourceRole())) {
                owner = memberRelation;
                break;
            }
        }
        boolean isAdmin = relationService.checkTenantAdmin(UserSessionLocal.getUser().getUserId());
        if (!isAdmin) {
            if (Objects.isNull(owner) || !UserSessionLocal.getUser().getUserId().equals(owner.getUserCode())) {
                throw new BizException("只有负责人可以修改");
            }
        }
        RequirementInfo requirementInfo = new RequirementInfo();
        requirementInfo.setId(interfaceSpaceDTO.getId());
        requirementInfo.setName(interfaceSpaceDTO.getName());
        requirementInfo.setRelatedRequirementCode(interfaceSpaceDTO.getCode());
        requirementInfo.setDescription(interfaceSpaceDTO.getDesc());
        requirementInfo.setSpaceName(interfaceSpaceDTO.getSpaceName());

        updateById(requirementInfo);

        //更新成员
        //全删了，再添加一遍
        for (MemberRelation memberRelation : memberRelationList) {
            memberRelation.setYn(0);
        }
        if (StringUtils.isEmpty(interfaceSpaceDTO.getOwner())) {
            interfaceSpaceDTO.setOwner(owner.getUserCode());
        }
        MemberRelation relation = newMemberRelation(interfaceSpaceDTO.getOwner(), requirementInfo.getId());
        relation.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
        memberRelationList.add(relation);
        for (String operator : interfaceSpaceDTO.getMembers()) {
            if (operator.equals(interfaceSpaceDTO.getOwner())) {
                //owner 不在成员列表
                continue;
            }
            MemberRelation relation2 = newMemberRelation(operator, requirementInfo.getId());
            memberRelationList.add(relation2);
        }
        relationService.saveOrUpdateBatch(memberRelationList);

        //添加日志
        requirementInfoLogService.createLog(requirementInfo.getId(), UserSessionLocal.getUser().getUserId(), new Date(), "更新了空间");

        return interfaceSpaceDTO.getId();
    }

    public IPage<MemberRelation> pageMembers(Long id, String search, Long current, Long size) {

        LambdaQueryWrapper<MemberRelation> lqw = new QueryWrapper<MemberRelation>().select("distinct user_code,resource_id,resource_type,resource_role,created,modified").lambda();
        lqw.eq(MemberRelation::getResourceId, id);
        lqw.eq(MemberRelation::getResourceType, ResourceTypeEnum.PRD_GROUP.getCode());
        lqw.eq(MemberRelation::getYn, 1);
        lqw.like(StringUtils.isNotEmpty(search), MemberRelation::getUserCode, search);
        lqw.orderByAsc(MemberRelation::getResourceType);
        Page<MemberRelation> page = relationService.page(new Page<>(current, size), lqw);
        relationService.fixRelationUsers(page.getRecords());
        return page;
    }

    public IPage<MemberRelation> getPageMembers(Long id, Long current, Long size) {

        LambdaQueryWrapper<MemberRelation> lqw = new QueryWrapper<MemberRelation>().select("distinct user_code,resource_id,resource_type,resource_role,created,modified").lambda();
        lqw.eq(MemberRelation::getResourceId, id);
        lqw.eq(MemberRelation::getResourceType, ResourceTypeEnum.PRD_GROUP.getCode());
        lqw.eq(MemberRelation::getYn, 1);
        lqw.orderByAsc(MemberRelation::getResourceRole);
        Page<MemberRelation> page = relationService.page(new Page<>(current, size), lqw);
        return page;
    }

    public MemberRelation getRequirementAdmin(Long id) {
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MemberRelation::getResourceId, id);
        lqw.eq(MemberRelation::getResourceType, ResourceTypeEnum.PRD_GROUP.getCode());
        lqw.eq(MemberRelation::getResourceRole, ResourceRoleEnum.ADMIN.getCode());
        lqw.eq(MemberRelation::getYn, 1);
        List<MemberRelation> members = relationService.list(lqw);

        if (members.isEmpty()) return null;
        relationService.fixRelationUsers(members);
        return members.get(0);
    }

    public List<MemberRelation> getMembers(Long id) {
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MemberRelation::getResourceId, id);
        lqw.eq(MemberRelation::getResourceType, ResourceTypeEnum.PRD_GROUP.getCode());
        lqw.eq(MemberRelation::getYn, 1);
        return relationService.list(lqw);
    }


    @Override
    public boolean removeMembers(Long id) {
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MemberRelation::getResourceId, id);
        lqw.eq(MemberRelation::getYn, 1);
        return relationService.remove(lqw);
    }

    @Transactional
    public Long deleteSpace(InterfaceSpaceDTO interfaceSpaceDTO) {
        List<MemberRelation> memberRelationList = getMembers(interfaceSpaceDTO.getId());
        MemberRelation owner = null;

        for (MemberRelation memberRelation : memberRelationList) {
            if (ResourceRoleEnum.ADMIN.getCode().equals(memberRelation.getResourceRole())) {
                owner = memberRelation;
                break;
            }
        }
        if (Objects.isNull(owner) || !UserSessionLocal.getUser().getUserId().equals(owner.getUserCode())) {
            throw new BizException("只有负责人可以修改");
        }
        RequirementInfo requirementInfo = new RequirementInfo();

        requirementInfo.setId(interfaceSpaceDTO.getId());
        requirementInfo.setYn(0);
        updateById(requirementInfo);
        //添加日志
        requirementInfoLogService.createLog(requirementInfo.getId(), UserSessionLocal.getUser().getUserId(), new Date(), "删除了空间");

        return interfaceSpaceDTO.getId();
    }


    public List<RequirementInfo> getRequirementByDemandCode(String demandCode) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(RequirementInfo::getRelatedRequirementCode, demandCode);
        lqw.eq(RequirementInfo::getType, RequirementTypeEnum.FLOW.getCode());
        lqw.eq(RequirementInfo::getYn, 1);
        lqw.eq(RequirementInfo::getCreator, UserSessionLocal.getUser().getUserId());

        return list(lqw);
    }

    public InterfaceSpaceDetailDTO getSpaceInfo(Long spaceId) {

        RequirementInfo requirementInfo = getById(spaceId);
        if (Objects.isNull(requirementInfo)) {
            throw new BizException("空间不存在");
        }
        if (requirementInfo.getYn() == 0) {
            throw new BizException("空间已经删除");
        }
        InterfaceSpaceDetailDTO interfaceSpaceDTO = new InterfaceSpaceDetailDTO();
        interfaceSpaceDTO.setId(spaceId);
        interfaceSpaceDTO.setDesc(requirementInfo.getDescription());
        interfaceSpaceDTO.setName(requirementInfo.getName());
        if (requirementInfo.getType() == 2 && StringUtils.isBlank(requirementInfo.getSpaceName())) {
            interfaceSpaceDTO.setSpaceName(requirementInfo.getName());
        } else {
            interfaceSpaceDTO.setSpaceName(requirementInfo.getSpaceName());
        }
        interfaceSpaceDTO.setCreated(requirementInfo.getCreated());
        interfaceSpaceDTO.setCreator(requirementInfo.getCreator());
        interfaceSpaceDTO.setModified(requirementInfo.getModified());
        interfaceSpaceDTO.setModifier(requirementInfo.getModifier());
        interfaceSpaceDTO.setCode(requirementInfo.getRelatedRequirementCode());
        interfaceSpaceDTO.setType(requirementInfo.getType());
        if (org.apache.commons.lang3.StringUtils.isEmpty(interfaceSpaceDTO.getCreator())) {
            interfaceSpaceDTO.setCreator("system");
        }
        if (org.apache.commons.lang3.StringUtils.isEmpty(interfaceSpaceDTO.getModifier())) {
            interfaceSpaceDTO.setModifier("system");
        }


        List<MemberRelation> memberRelationList = getMembers(spaceId);
        MemberRelation owner = null;
        for (MemberRelation memberRelation : memberRelationList) {
            if (memberRelation.getResourceRole().equals(ResourceRoleEnum.ADMIN.getCode())) {
                owner = memberRelation;
                break;
            }
        }
        if (Objects.nonNull(owner)) {
            memberRelationList.remove(owner);
        } else {
            if (StringUtils.isNotEmpty(requirementInfo.getCreator())) {
                owner = new MemberRelation();
                owner.setUserCode(requirementInfo.getCreator());
            }
        }

        List<UserInfoDTO> userInfoDTOList = memberRelationList.stream().map(this::getUser).distinct().collect(Collectors.toList());
        interfaceSpaceDTO.setMembers(userInfoDTOList);
        interfaceSpaceDTO.setOwner(getUser(owner));

        // 判断是否是japi_admin
        boolean isAdmin = false;
        try {
            UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());
            isAdmin = userRoleDTO.getJapiAdmin();
        } catch (Exception e) {
            log.error("getSpaceInfo.queryUserRole error", e);
        }
        if (isAdmin) {
            interfaceSpaceDTO.setIsOwner(Boolean.TRUE);
        } else {
            if (Objects.isNull(interfaceSpaceDTO.getOwner())) {
                interfaceSpaceDTO.setIsOwner(Boolean.FALSE);
            } else {
                interfaceSpaceDTO.setIsOwner(UserSessionLocal.getUser().getUserId().equals(interfaceSpaceDTO.getOwner().getErp()));
            }
        }

        return interfaceSpaceDTO;


    }


    private UserInfoDTO getUser(MemberRelation memberRelation) {
        if (Objects.isNull(memberRelation)) {
            return null;
        }
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setErp(memberRelation.getUserCode());
        UserVo userVo = userHelper.getUserBaseInfoByUserName(memberRelation.getUserCode());
        if(Objects.nonNull(userVo)){
            userInfoDTO.setDeptName(userVo.getOrganizationFullName());
            userInfoDTO.setUserName(org.apache.commons.lang.StringUtils.isNotEmpty(userVo.getRealName()) ? userVo.getRealName() : memberRelation.getUserCode());
            userInfoDTO.setModified(memberRelation.getModified());
        }
        return userInfoDTO;
    }

    /**
     * 获取空间统计信息
     *
     * @param spaceId
     * @return
     */
    public InterfaceSpaceStaticDTO getSpaceInfoStatic(Long spaceId) {
        InterfaceSpaceStaticDTO result = new InterfaceSpaceStaticDTO();
        InterfaceSpaceDetailDTO interfaceSpaceDetailDTO = getSpaceInfo(spaceId);
        BeanUtils.copyProperties(interfaceSpaceDetailDTO, result);

        //http接口统计
        LambdaQueryWrapper<RequirementInterfaceGroup> lqwGroup = new LambdaQueryWrapper<>();
        lqwGroup.eq(RequirementInterfaceGroup::getRequirementId, spaceId).eq(RequirementInterfaceGroup::getInterfaceType, InterfaceTypeEnum.HTTP.getCode());
        lqwGroup.select(RequirementInterfaceGroup::getInterfaceId, RequirementInterfaceGroup::getSortGroupTree);
        Integer httpGroupCount = 0;

        List<RequirementInterfaceGroup> httpRequirementInterfaceGroupList = requirementInterfaceGroupService.list(lqwGroup);
        if (CollectionUtils.isNotEmpty(httpRequirementInterfaceGroupList)) {
            httpGroupCount = httpRequirementInterfaceGroupList.size();
        }
        Integer httpMethodCount = 0;
        for (RequirementInterfaceGroup requirementInterfaceGroup : httpRequirementInterfaceGroupList) {
            if (Objects.nonNull(requirementInterfaceGroup.getSortGroupTree())) {
                httpMethodCount = httpMethodCount + requirementInterfaceGroup.getSortGroupTree().allMethods().size();
            }
        }


        //jsf 接口统计
        LambdaQueryWrapper<RequirementInterfaceGroup> lqwJsfGroup = new LambdaQueryWrapper<>();
        lqwJsfGroup.eq(RequirementInterfaceGroup::getRequirementId, spaceId);
        lqwJsfGroup.select(RequirementInterfaceGroup::getInterfaceId, RequirementInterfaceGroup::getSortGroupTree);
        lqwJsfGroup.eq(RequirementInterfaceGroup::getInterfaceType, InterfaceTypeEnum.JSF.getCode());
        Integer jsfGroupCount = 0;


        List<RequirementInterfaceGroup> jsfRequirementInterfaceGroupList = requirementInterfaceGroupService.list(lqwJsfGroup);
        if (CollectionUtils.isNotEmpty(jsfRequirementInterfaceGroupList)) {
            jsfGroupCount = jsfRequirementInterfaceGroupList.size();
        }

        Integer jsfMethodCount = 0;
        for (RequirementInterfaceGroup requirementInterfaceGroup : jsfRequirementInterfaceGroupList) {
            if (Objects.nonNull(requirementInterfaceGroup.getSortGroupTree())) {
                jsfMethodCount = jsfMethodCount + requirementInterfaceGroup.getSortGroupTree().allMethods().size();
            }
        }


        //环境数统计
        LambdaQueryWrapper<EnvConfig> lqwEnv = new LambdaQueryWrapper<>();
        lqwEnv.eq(BaseEntity::getYn, 1).eq(EnvConfig::getRequirementId, spaceId);

        Integer envCount = envConfigService.count(lqwEnv);

        //人员数
        Integer userCount = 0;
        if (Objects.nonNull(result.getOwner())) {
            userCount = userCount + 1;
        }
        if (CollectionUtils.isNotEmpty(result.getMembers())) {
            userCount = userCount + result.getMembers().size();
            if (result.getMembers().contains(result.getOwner())) {
                userCount = userCount - 1;
            }
        }

        //空间日志
        LambdaQueryWrapper<RequirementInfoLog> lqwLog = new LambdaQueryWrapper<>();
        lqwLog.eq(RequirementInfoLog::getRequirementId, spaceId);
        lqwLog.orderByDesc(BaseEntityNoDelLogic::getCreated);
        lqwLog.last("limit 1");

        List<RequirementInfoLog> requirementInfoLogList = requirementInfoLogService.list(lqwLog);


        result.setHttpGroupCount(httpGroupCount);
        result.setJsfCount(jsfGroupCount);
        result.setHttpCount(httpMethodCount);
        result.setMethodCount(jsfMethodCount);
        result.setUserCount(userCount);
        result.setEnvCount(envCount);

        result.setTotalMethodCount(httpMethodCount + jsfMethodCount);
        result.setLastLog("");
        if (CollectionUtils.isNotEmpty(requirementInfoLogList)) {
            result.setLastLog(requirementInfoLogList.get(0).getDesc());
        }


        return result;


    }

    /**
     * @param spaceId
     * @return
     */
    public InterfaceSpaceUser getSpaceUser(Long spaceId) {
        InterfaceSpaceUser interfaceSpaceUser = new InterfaceSpaceUser();
        // 只查前500成员，大于500的项目属于demo项目，会导致查询很慢
        IPage<MemberRelation> memberRelationPage = getPageMembers(spaceId, 1l, 500l);
        MemberRelation owner = null;
        for (MemberRelation memberRelation : memberRelationPage.getRecords()) {
            if (memberRelation.getResourceRole().equals(ResourceRoleEnum.ADMIN.getCode())) {
                owner = memberRelation;
                break;
            }
        }
        memberRelationPage.getRecords().remove(owner);

        List<UserInfoDTO> userInfoDTOList = memberRelationPage.getRecords().stream().map(this::getUser).collect(Collectors.toList());
        interfaceSpaceUser.setMembers(userInfoDTOList);
        if (Objects.nonNull(owner)) {
            interfaceSpaceUser.setOwner(getUser(owner));
        }
        return interfaceSpaceUser;
    }

    public InterfaceSpaceUser pageSpaceUser(Long spaceId, String search, Long current, Long size) {
        InterfaceSpaceUser interfaceSpaceUser = new InterfaceSpaceUser();
        IPage<MemberRelation> memberRelationList = pageMembers(spaceId, search, current, size);

        MemberRelation owner = getRequirementAdmin(spaceId);
        /*for (MemberRelation memberRelation : memberRelationList) {
            if (memberRelation.getResourceRole().equals(ResourceRoleEnum.ADMIN.getCode())) {
                owner = memberRelation;
                break;
            }
        }
        memberRelationList.remove(owner);*/
        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());

        List<UserInfoDTO> userInfoDTOList = memberRelationList.getRecords().stream().map(item -> UserInfoDTO.from(item)).distinct().collect(Collectors.toList());

        if (userRoleDTO.getJapiAdmin() || userRoleDTO.getJapiDepartment() || userRoleDTO.getDeptLeader()) {
            UserInfoDTO userInfoDTO = new UserInfoDTO();
            userInfoDTO.setErp(userRoleDTO.getErp());
            userInfoDTO.setUserName(userRoleDTO.getUserName());
            userInfoDTO.setDeptName(userRoleDTO.getDept());
            userInfoDTOList.add(userInfoDTO);
        }
        interfaceSpaceUser.setMembers(userInfoDTOList);
        interfaceSpaceUser.setTotal(memberRelationList.getTotal());
        if (Objects.nonNull(owner)) {
            interfaceSpaceUser.setOwner(UserInfoDTO.from(owner));
        }
        return interfaceSpaceUser;
    }

    @Transactional
    @Override
    public Boolean removeUser(RemoveSpaceUserDTO removeSpaceUser) {
        if (CollectionUtils.isEmpty(removeSpaceUser.getUserErpList())) {
            return true;
        }
        List<MemberRelation> memberRelationList = getMembers(removeSpaceUser.getSpaceId());
        MemberRelation owner = null;
        for (MemberRelation memberRelation : memberRelationList) {
            if (memberRelation.getResourceRole().equals(ResourceRoleEnum.ADMIN.getCode())) {
                owner = memberRelation;
                break;
            }
        }
        memberRelationList.remove(owner);
        if (Objects.isNull(owner) || !UserSessionLocal.getUser().getUserId().equals(owner.getUserCode())) {
            throw new BizException("只有负责人可以删除");
        }
        List<MemberRelation> updateList = Lists.newArrayList();
        Map<String, MemberRelation> memberRelationMap = memberRelationList.stream().collect(Collectors.toMap(MemberRelation::getUserCode, memberRelation -> memberRelation));
        for (String erp : removeSpaceUser.getUserErpList()) {
            if (memberRelationMap.containsKey(erp)) {
                MemberRelation memberRelation = memberRelationMap.get(erp);
                memberRelation.setYn(0);
                updateList.add(memberRelation);
            }
        }
        Boolean result = relationService.updateBatchById(updateList);

        //添加日志
        for (MemberRelation user : updateList) {
            requirementInfoLogService.createLog(removeSpaceUser.getSpaceId(), UserSessionLocal.getUser().getUserId(), new Date(), "移除了成员：" + user.getUserCode());

        }

        return result;

    }

    @Transactional
    @Override
    public Boolean addUser(AddSpaceUserDTO addSpaceUserDTO) {
        RequirementInfo requirementInfo = getById(addSpaceUserDTO.getSpaceId());
        List<MemberRelation> memberRelationList = getMembers(addSpaceUserDTO.getSpaceId());
        MemberRelation owner = null;
        for (MemberRelation memberRelation : memberRelationList) {
            if (memberRelation.getResourceRole().equals(ResourceRoleEnum.ADMIN.getCode())) {
                owner = memberRelation;
                break;
            }
        }
        if (Objects.nonNull(owner)) {
            memberRelationList.remove(owner);
        } else {
            if (StringUtils.isNotEmpty(requirementInfo.getCreator())) {
                owner = new MemberRelation();
                owner.setUserCode(requirementInfo.getCreator());
            }
        }
        Boolean isAdmin = false;
        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());
        if (userRoleDTO.getJapiAdmin()) {
            //管理员可以看到所有数据。
            isAdmin = true;
        } else if (userRoleDTO.getDeptLeader()) {
            //部门负责人可以看到所属部门所有数据
            isAdmin = true;
        }


        if (Objects.isNull(owner) || (!UserSessionLocal.getUser().getUserId().equals(owner.getUserCode()) && !isAdmin)) {
            throw new BizException("只有负责人可以删除");
        }
        List<MemberRelation> updateList = Lists.newArrayList();

        //更新负责人
        if (StringUtils.isNotEmpty(addSpaceUserDTO.getOwner())) {
            owner.setUserCode(addSpaceUserDTO.getOwner());
            updateList.add(owner);
            requirementInfoLogService.createLog(addSpaceUserDTO.getSpaceId(), UserSessionLocal.getUser().getUserId(), new Date(), "管理员设置为成员：" + addSpaceUserDTO.getOwner());
        }
        //更新成员

        if (CollectionUtils.isNotEmpty(addSpaceUserDTO.getUserErpList())) {
            Map<String, MemberRelation> memberRelationMap = Maps.newHashMap();
            for (MemberRelation memberRelation : memberRelationList) {
                if (!memberRelationMap.containsKey(memberRelation.getUserCode())) {
                    memberRelationMap.put(memberRelation.getUserCode(), memberRelation);
                }
            }


            for (String erp : addSpaceUserDTO.getUserErpList()) {
                if (!memberRelationMap.containsKey(erp)) {
                    MemberRelation relation = newMemberRelation(erp, addSpaceUserDTO.getSpaceId());
                    updateList.add(relation);
                    requirementInfoLogService.createLog(addSpaceUserDTO.getSpaceId(), UserSessionLocal.getUser().getUserId(), new Date(), "添加了成员：" + erp);
                }

            }
        }
        if (updateList.isEmpty()) return true;
        return relationService.saveOrUpdateBatch(updateList);
    }

    @Override
    public InterfaceSpaceUser checkUser(Long spaceId, String erp) {
        InterfaceSpaceUser interfaceSpaceUser = new InterfaceSpaceUser();
        interfaceSpaceUser.setInclude(false);
        UserRoleDTO userRoleDTO = accRoleServiceAdapter.queryUserRole(UserSessionLocal.getUser().getUserId());
        Boolean isAdmin = false;
        if (userRoleDTO.getJapiAdmin()) {
            //管理员可以看到所有数据。
            interfaceSpaceUser.setInclude(true);
            isAdmin = true;
        } else if (userRoleDTO.getDeptLeader()) {
            //部门负责人可以看到所属部门所有数据
            interfaceSpaceUser.setInclude(true);
            isAdmin = true;
        }

        List<MemberRelation> memberRelationList = getMembers(spaceId);
        if (!interfaceSpaceUser.isInclude()) {
            for (MemberRelation memberRelation : memberRelationList) {
                if (memberRelation.getUserCode().equals(erp)) {
                    interfaceSpaceUser.setInclude(true);
                    break;

                }
            }
        }

        MemberRelation owner = null;

        for (MemberRelation memberRelation : memberRelationList) {
            if (ResourceRoleEnum.ADMIN.getCode().equals(memberRelation.getResourceRole())) {
                owner = memberRelation;
                break;
            }
        }
        if (isAdmin) {
            owner.setUserCode(UserSessionLocal.getUser().getUserId());
        }

        interfaceSpaceUser.setOwner(getUser(owner));

        return interfaceSpaceUser;
    }

    @Override
    public Page<InterfaceSpaceDetailDTO> querySpaceList(InterfaceSpaceParam interfaceSpaceParam) {
        LambdaQueryWrapper<RequirementInfo> lqw = new LambdaQueryWrapper<>();
        lqw.eq(BaseEntity::getYn, 1);
        lqw.eq(RequirementInfo::getType, RequirementTypeEnum.JAPI.getCode());
        if (org.apache.commons.lang3.StringUtils.isNotEmpty(interfaceSpaceParam.getSpaceName())) {
            lqw.like(RequirementInfo::getName, interfaceSpaceParam.getSpaceName());
        }

        LambdaQueryWrapper<MemberRelation> lqwMember = new LambdaQueryWrapper<>();
        lqwMember.eq(MemberRelation::getUserCode, UserSessionLocal.getUser().getUserId());
        lqwMember.eq(MemberRelation::getResourceType, ResourceTypeEnum.PRD_GROUP.getCode());
        lqwMember.eq(MemberRelation::getYn, 1);
        lqwMember.select(MemberRelation::getResourceId);
        List<MemberRelation> memberRelationList = relationService.list(lqwMember);
        if (CollectionUtils.isEmpty(memberRelationList)) {
            Page<InterfaceSpaceDetailDTO> page = new Page<>();
            page.setTotal(0L);
            return page;
        }
        List<Long> spaceIdList = memberRelationList.stream().map(MemberRelation::getResourceId).collect(Collectors.toList());
        lqw.in(RequirementInfo::getId, spaceIdList);
        lqw.orderByDesc(BaseEntityNoDelLogic::getCreated);
        Page page = new Page<>(interfaceSpaceParam.getCurrent(), interfaceSpaceParam.getSize());
        Page<RequirementInfo> requirementInfoPage = page(page, lqw);

        Page<InterfaceSpaceDetailDTO> result = new Page<>();
        result.setTotal(requirementInfoPage.getTotal());
        result.setCurrent(requirementInfoPage.getCurrent());
        result.setSize(requirementInfoPage.getSize());
        List<InterfaceSpaceDetailDTO> interfaceSpaceDetailDTOList = Lists.newArrayList();
        result.setRecords(interfaceSpaceDetailDTOList);
        if (CollectionUtils.isNotEmpty(requirementInfoPage.getRecords())) {
            for (RequirementInfo record : requirementInfoPage.getRecords()) {
                InterfaceSpaceDetailDTO interfaceSpaceDetailDTO = new InterfaceSpaceDetailDTO();
                InterfaceSpaceUser interfaceSpaceUser = getSpaceUser(record.getId());
                interfaceSpaceDetailDTO.setOwner(interfaceSpaceUser.getOwner());
                interfaceSpaceDetailDTO.setMembers(interfaceSpaceUser.getMembers());
                interfaceSpaceDetailDTO.setId(record.getId());
                interfaceSpaceDetailDTO.setName(record.getName());
                interfaceSpaceDetailDTO.setDesc(record.getDescription());
                interfaceSpaceDetailDTO.setCreated(record.getCreated());
                if (Objects.isNull(interfaceSpaceUser.getOwner())) {
                    interfaceSpaceDetailDTO.setIsOwner(false);
                } else {
                    interfaceSpaceDetailDTO.setIsOwner(UserSessionLocal.getUser().getUserId().equals(interfaceSpaceUser.getOwner().getErp()));
                }
                interfaceSpaceDetailDTOList.add(interfaceSpaceDetailDTO);

            }
        }

        return result;
    }

    @Override
    public Boolean updateOwner(AddSpaceUserDTO addSpaceUserDTO) {
        List<MemberRelation> memberRelationList = getMembers(addSpaceUserDTO.getSpaceId());
        MemberRelation owner = null;
        for (MemberRelation memberRelation : memberRelationList) {
            if (memberRelation.getResourceRole().equals(ResourceRoleEnum.ADMIN.getCode())) {
                owner = memberRelation;
                break;
            }
        }
        if (Objects.isNull(owner)) {
            owner = newMemberRelation(addSpaceUserDTO.getOwner(), addSpaceUserDTO.getSpaceId());
            owner.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
        } else {
            owner.setUserCode(addSpaceUserDTO.getOwner());
        }
        if (CollectionUtils.isEmpty(addSpaceUserDTO.getUserErpList())) {
            return false;
        }

        return relationService.saveOrUpdate(owner);
    }


}
