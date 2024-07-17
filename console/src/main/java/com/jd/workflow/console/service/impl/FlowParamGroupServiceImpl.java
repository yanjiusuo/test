package com.jd.workflow.console.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.common.util.StringUtils;
import com.jd.workflow.console.base.EmptyUtil;
import com.jd.workflow.console.base.ServiceException;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.ResourceRoleEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.base.enums.ServiceErrorEnum;
import com.jd.workflow.console.dao.mapper.FlowParamGroupMapper;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.dto.MemberRelationWithUser;
import com.jd.workflow.console.dto.UserForAddDTO;
import com.jd.workflow.console.dto.UserInfoDTO;
import com.jd.workflow.console.dto.flow.param.FlowParamGroupDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamGroupReqDTO;
import com.jd.workflow.console.dto.flow.param.QueryParamGroupResultDTO;
import com.jd.workflow.console.entity.FlowParam;
import com.jd.workflow.console.entity.FlowParamGroup;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.service.IFlowParamGroupService;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 16:51
 * @Description:
 */
@Service
@Slf4j
public class FlowParamGroupServiceImpl extends ServiceImpl<FlowParamGroupMapper, FlowParamGroup> implements IFlowParamGroupService {


    @Resource
    private FlowParamServiceImpl flowParamServiceImpl;

    /**
     * 资源关系表
     *
     * @date: 2022/5/16 11:09
     * @author wubaizhao1
     */
    @Resource
    IMemberRelationService memberRelationService;

    /**
     * 用户表
     *
     * @date: 2022/6/1 16:57
     * @author wubaizhao1
     */
    @Resource
    IUserInfoService userInfoService;

    @Override
    public Long addGroup(FlowParamGroupDTO dto) {
        // 查询是否已存在 校验：名称
        addCheckDuplicate(dto);
        FlowParamGroup flowParamGroup = new FlowParamGroup();
        flowParamGroup.setName(dto.getGroupName());
        Date opTime = new Date();
        flowParamGroup.setCreated(opTime);
        flowParamGroup.setModified(opTime);
        flowParamGroup.setCreator(UserSessionLocal.getUser().getUserId());
        flowParamGroup.setModifier(UserSessionLocal.getUser().getUserId());
        flowParamGroup.setYn(DataYnEnum.VALID.getCode());
        boolean save = save(flowParamGroup);
        if (!save) {
            log.error("FlowParamGroupServiceImpl addGroup error!! groupName:{}", dto.getGroupName());
        } else {
            addAdmin(ResourceTypeEnum.PARAM_GROUP, UserSessionLocal.getUser().getUserId(), flowParamGroup.getId());
        }
        return flowParamGroup.getId();
    }


    @Override
    public Boolean editGroup(FlowParamGroupDTO dto) {
        checkAuth(getFlowParamGroupById(dto.getId()));
        FlowParamGroup flowParamGroup = new FlowParamGroup();
        flowParamGroup.setId(dto.getId());
        flowParamGroup.setName(dto.getGroupName());
        Date opTime = new Date();
        flowParamGroup.setModified(opTime);
        flowParamGroup.setModifier(UserSessionLocal.getUser().getUserId());
        return updateById(flowParamGroup);
    }

    @Override
    public Boolean removeGroup(Long id) {
        FlowParamGroup flowParamGroup = getFlowParamGroupById(id);
        checkAuth(flowParamGroup);
        checkCanRemoveGroup(id);
        return removeById(id);
    }


    @Override
    public QueryParamGroupResultDTO queryGroup(QueryParamGroupReqDTO query) {
        QueryParamGroupResultDTO resultDTO = new QueryParamGroupResultDTO();
        resultDTO.setCurrentPage(query.getCurrentPage());
        resultDTO.setPageSize(query.getPageSize());
        query.initPageParam(10000);
        // 查询当前用户有权限的分组ids
        List<Long> userResourceList = getUserResourceList();
        if (CollectionUtils.isEmpty(userResourceList)) {
            //当前用户无可查询的分组信息
            return resultDTO;
        }
        if (Objects.nonNull(query.getId()) && userResourceList.contains(query.getId())) {
            userResourceList.clear();
            userResourceList.add(query.getId());
        }
        // 按照创建时间正序排序
        LambdaQueryWrapper<FlowParamGroup> qw = Wrappers.<FlowParamGroup>lambdaQuery().orderByAsc(FlowParamGroup::getCreated);
        qw.like(StringUtils.isNotBlank(query.getGroupName()), FlowParamGroup::getName, query.getGroupName());
        qw.in(FlowParamGroup::getId, userResourceList);
        Page<FlowParamGroup> pageResult = page(new Page(query.getCurrentPage(), query.getPageSize()), qw);
        if (pageResult != null && CollectionUtils.isNotEmpty(pageResult.getRecords())) {
            resultDTO.setTotalCnt(pageResult.getTotal());
            resultDTO.setList(pageResult.getRecords().stream().map(o -> {
                FlowParamGroup dto = new FlowParamGroup();
                BeanUtils.copyProperties(o, dto);
                return dto;
            }).collect(Collectors.toList()));
        }
        return resultDTO;
    }

    @Override
    public FlowParamGroup getGroupByGroupId(Long groupId) {
        FlowParamGroup lastObj = this.getOne(Wrappers.<FlowParamGroup>lambdaQuery().eq(FlowParamGroup::getId, groupId).eq(FlowParamGroup::getYn, DataYnEnum.VALID.getCode()));
        return lastObj;
    }

    @Override
    public List<MemberRelationWithUser> listMember(Long groupId) {
        FlowParamGroup flowParamGroup = getGroupByGroupId(groupId);
        if (Objects.isNull(flowParamGroup)) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        //权限数据+用户数据
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(ResourceTypeEnum.PARAM_GROUP.getCode());
        memberRelationDTO.setResourceId(groupId);
        memberRelationDTO.setResourceRoleList(Arrays.asList(ResourceRoleEnum.MEMBER.getCode(), ResourceRoleEnum.ADMIN.getCode()));
        List<MemberRelationWithUser> withUserList = memberRelationService.listRelationWithUserInfoByResource(memberRelationDTO);
        return withUserList;
    }

    @Override
    public List<UserForAddDTO> listMemberForAdd(Long groupId, String userCode) {
        //分组数据
        FlowParamGroup flowParamGroup = getGroupByGroupId(groupId);
        if (Objects.isNull(flowParamGroup)) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        //模糊搜索用户数据
        UserInfoDTO userInfoDTO = new UserInfoDTO();
        userInfoDTO.setUserCode(userCode);
        List<UserInfo> userInfos = userInfoService.listByCode(userInfoDTO);
        //权限数据+用户数据
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(ResourceTypeEnum.PARAM_GROUP.getCode());
        memberRelationDTO.setResourceId(groupId);
        memberRelationDTO.setResourceRoleList(Arrays.asList(ResourceRoleEnum.MEMBER.getCode(), ResourceRoleEnum.ADMIN.getCode()));
        List<String> userCodeList = memberRelationService.listUserCodeByResource(memberRelationDTO);
        Set<String> userCodeSet = new HashSet<>(userCodeList);
        List<UserForAddDTO> result = new ArrayList<>();
        for (UserInfo userInfo : userInfos) {
            UserForAddDTO userForAddDTO = new UserForAddDTO();
            BeanUtils.copyProperties(userInfo, userForAddDTO);
            if (userCodeSet.contains(userInfo.getUserCode())) {
                userForAddDTO.setExist(true);
            } else {
                userForAddDTO.setExist(false);
            }
            result.add(userForAddDTO);
        }
        return result;
    }

    @Override
    public Boolean addMember(Long groupId, String userCode) {
        //分组数据
        FlowParamGroup flowParamGroup = getGroupByGroupId(groupId);
        if (Objects.isNull(flowParamGroup)) {
            throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(ResourceTypeEnum.PARAM_GROUP.getCode());
        memberRelationDTO.setResourceId(groupId);
        memberRelationDTO.setUserCode(userCode);
        memberRelationDTO.setResourceRole(ResourceRoleEnum.MEMBER.getCode());
        Boolean binding = memberRelationService.binding(memberRelationDTO);
        return binding;
    }

    /**
     * 校验分组名称是否已存在
     *
     * @param dto
     */
    private void addCheckDuplicate(FlowParamGroupDTO dto) {
        LambdaQueryWrapper<FlowParamGroup> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FlowParamGroup::getYn, DataYnEnum.VALID.getCode())
                .eq(FlowParamGroup::getCreator, UserSessionLocal.getUser().getUserId())
                .eq(FlowParamGroup::getName, dto.getGroupName());
        int count = count(lqw);
        if (count > 0) {
            throw ServiceException.withCommon("该名称已存在");
        }
    }


    private void checkAuth(FlowParamGroup flowParamGroup) {
        if (UserSessionLocal.getUser().getUserId() == null) return;
        if (!Objects.equals(flowParamGroup.getCreator(), UserSessionLocal.getUser().getUserId())) {
            throw new BizException("无分组操作权限!");
        }
    }

    /**
     * 根据id查已存在的对象数据
     *
     * @param id
     * @return
     */
    private FlowParamGroup getFlowParamGroupById(Long id) {
        FlowParamGroup lastObj = this.getOne(Wrappers.<FlowParamGroup>lambdaQuery().eq(FlowParamGroup::getId, id).eq(FlowParamGroup::getYn, DataYnEnum.VALID.getCode()));
        if (lastObj == null) {
            throw new BizException("分组不存在!");
        }
        return lastObj;
    }


    /**
     * 添加管理员
     *
     * @param resourceType
     * @param adminCode
     * @param id
     */
    private void addAdmin(ResourceTypeEnum resourceType, String adminCode, Long id) {
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(resourceType.getCode());
        memberRelationDTO.setResourceId(id);
        memberRelationDTO.setUserCode(adminCode);
        memberRelationDTO.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
        memberRelationService.binding(memberRelationDTO);
    }

    /**
     * 添加成员
     *
     * @param resourceType
     * @param adminCode
     * @param id
     */
    private void addMember(ResourceTypeEnum resourceType, String adminCode, Long id) {
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(resourceType.getCode());
        memberRelationDTO.setResourceId(id);
        memberRelationDTO.setUserCode(adminCode);
        memberRelationDTO.setResourceRole(ResourceRoleEnum.MEMBER.getCode());
        memberRelationService.binding(memberRelationDTO);
    }


    /**
     * 获取改用户有权限的分组ids
     *
     * @return
     */
    public List<Long> getUserResourceList() {
        List<Long> resourceIds = new ArrayList<>();
        String userCode = UserSessionLocal.getUser().getUserId();
        if (EmptyUtil.isNotEmpty(userCode)) {
            MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
            memberRelationDTO.setResourceType(ResourceTypeEnum.PARAM_GROUP.getCode());
            memberRelationDTO.setUserCode(userCode);
            log.info("FlowParamGroupServiceImpl getUserResourceList userCode is not empty memberRelationDTO={}", JsonUtils.toJSONString(memberRelationDTO));
            resourceIds = memberRelationService.listResourceIds(memberRelationDTO);
        }
        return resourceIds;
    }

    /**
     * 校验该分组是否可删除
     * 分组下存在param不可删除
     *
     * @param id
     */
    private void checkCanRemoveGroup(Long id) {
        LambdaQueryWrapper<FlowParam> lqw = new LambdaQueryWrapper<>();
        lqw.eq(FlowParam::getYn, DataYnEnum.VALID.getCode())
                .eq(FlowParam::getGroupId, id);
        int count = flowParamServiceImpl.count(lqw);
        if (count > 0) {
            throw ServiceException.withCommon("分组下有数据，无法删除，请先删除所有分组下数据后重试！");
        }
    }

}
