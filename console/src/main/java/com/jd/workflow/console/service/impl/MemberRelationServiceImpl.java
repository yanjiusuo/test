package com.jd.workflow.console.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.jd.workflow.console.base.EmptyUtil;
import com.jd.workflow.console.base.ServiceException;
import com.jd.workflow.console.base.enums.*;
import com.jd.workflow.console.dao.mapper.MemberRelationMapper;
import com.jd.workflow.console.dto.AppInfoDTO;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.dto.MemberRelationWithUser;
import com.jd.workflow.console.entity.AppInfo;
import com.jd.workflow.console.entity.InterfaceManage;
import com.jd.workflow.console.entity.MemberRelation;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.helper.MaskPinHelper;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IMemberRelationService;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 接口成员关联表 服务实现类
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Slf4j
@Service
public class MemberRelationServiceImpl extends ServiceImpl<MemberRelationMapper, MemberRelation> implements IMemberRelationService {

    /**
     * @date: 2022/5/16 14:45
     * @author wubaizhao1
     */
    @Resource
    private MemberRelationMapper memberRelationMapper;
    /**
     * 用户信息
     *
     * @date: 2022/5/16 14:44
     * @author wubaizhao1
     */
    @Resource
    private IUserInfoService userInfoService;

    private static final Long DEFAULT = 0L;
    /**
     * 是否存在租户管理员,如果不存在,则直接返回false,减少sql压力
     */
    @Value("${memberRelation.checkTenantAdminFlag}")
    private Boolean checkTenantAdminFlag = false;

    @Resource
    private MaskPinHelper maskPinHelper;
    @Autowired
    IAppInfoService appInfoService;

    /**
     * 绑定资源
     * 入参：[逻辑唯一索引] + 权限角色
     * [逻辑唯一索引]: 用户Code 资源id 资源类型
     * 若是管理员，则是 用户Code + 资源id=0 类型=租户管理员
     *
     * @param memberRelationDTO
     * @return
     * @date: 2022/5/12 18:38
     * @author wubaizhao1
     */
    @Override
    public Boolean binding(MemberRelationDTO memberRelationDTO) {
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(memberRelationDTO.getUserCode(), "用户Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceRole(), "资源角色不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        //校验租户管理员
        ResourceRoleEnum resourceRoleEnum = ResourceRoleEnum.getByCode(memberRelationDTO.getResourceRole());
        if (resourceRoleEnum != ResourceRoleEnum.TENANT_ADMIN && EmptyUtil.isEmpty(memberRelationDTO.getResourceId())) {
            throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "资源id不能为空");
        }
        if (resourceRoleEnum != ResourceRoleEnum.TENANT_ADMIN && EmptyUtil.isEmpty(memberRelationDTO.getResourceType())) {
            throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "资源类型不能为空");
        }
        if (resourceRoleEnum == ResourceRoleEnum.TENANT_ADMIN) {
            memberRelationDTO.setResourceId(DEFAULT);
            memberRelationDTO.setResourceType(ResourceTypeEnum.TENANT_ADMIN.getCode());
        }
        // 查询是否已存在 [逻辑唯一索引]:  用户Code 资源id 资源类型
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode())
                .eq(MemberRelation::getUserCode, memberRelationDTO.getUserCode())
                .eq(MemberRelation::getResourceType, memberRelationDTO.getResourceType())
                .eq(MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        int count = memberRelationMapper.selectCount(lqw);
        if (count > 0) {
            throw ServiceException.with(ServiceErrorEnum.DATA_DUPLICATION_ERROR);
        }
        //添加
        MemberRelation entity = new MemberRelation();
        BeanUtils.copyProperties(memberRelationDTO, entity);
        entity.setYn(DataYnEnum.VALID.getCode());
        int add = memberRelationMapper.insert(entity);
        return add > 0;
    }

    @Override
    public Boolean checkAndBinding(MemberRelationDTO memberRelationDTO) {
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(memberRelationDTO.getUserCode(), "用户Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceRole(), "资源角色不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        //校验租户管理员
        ResourceRoleEnum resourceRoleEnum = ResourceRoleEnum.getByCode(memberRelationDTO.getResourceRole());
        if (resourceRoleEnum != ResourceRoleEnum.TENANT_ADMIN && EmptyUtil.isEmpty(memberRelationDTO.getResourceId())) {
            throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "资源id不能为空");
        }
        if (resourceRoleEnum != ResourceRoleEnum.TENANT_ADMIN && EmptyUtil.isEmpty(memberRelationDTO.getResourceType())) {
            throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "资源类型不能为空");
        }
        if (resourceRoleEnum == ResourceRoleEnum.TENANT_ADMIN) {
            memberRelationDTO.setResourceId(DEFAULT);
            memberRelationDTO.setResourceType(ResourceTypeEnum.TENANT_ADMIN.getCode());
        }
        // 查询是否已存在 [逻辑唯一索引]:  用户Code 资源id 资源类型
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode())
                .eq(MemberRelation::getUserCode, memberRelationDTO.getUserCode())
                .eq(MemberRelation::getResourceType, memberRelationDTO.getResourceType())
                .eq(MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        int count = memberRelationMapper.selectCount(lqw);
        if (count > 0) {
            return true;
        }
        //添加
        MemberRelation entity = new MemberRelation();
        BeanUtils.copyProperties(memberRelationDTO, entity);
        entity.setYn(DataYnEnum.VALID.getCode());
        int add = memberRelationMapper.insert(entity);
        return add > 0;
    }

    /**
     * 更换资源绑定的负责人-》管理员
     *
     * @date: 2022/6/1 10:29
     * @author wubaizhao1
     */
    @Override
    public Boolean changeAdminCode(MemberRelationDTO memberRelationDTO) {
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(memberRelationDTO.getUserCode(), "用户Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceId(), "资源id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceType(), "资源类型不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        memberRelationDTO.setResourceRole(ResourceRoleEnum.ADMIN.getCode());

        {
            LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
            lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode())
                    .eq(MemberRelation::getResourceType, memberRelationDTO.getResourceType())
                    .eq(MemberRelation::getUserCode,memberRelationDTO.getUserCode())
                    .eq(MemberRelation::getResourceId, memberRelationDTO.getResourceId());
            remove(lqw);
        }

        // 查询负责人 ->
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode())
                .eq(MemberRelation::getResourceRole, memberRelationDTO.getResourceRole())
                .eq(MemberRelation::getResourceType, memberRelationDTO.getResourceType())
                .eq(MemberRelation::getYn, DataYnEnum.VALID.getCode())
                .eq(MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        MemberRelation memberRelation = memberRelationMapper.selectOne(lqw);
        if (memberRelation == null) {
            MemberRelation relation = new MemberRelation();
            relation.setYn(1);
            relation.setUserCode(memberRelationDTO.getUserCode());
            relation.setResourceId(memberRelationDTO.getResourceId());
            relation.setResourceType(memberRelationDTO.getResourceType());
            relation.setResourceRole(ResourceRoleEnum.ADMIN.getCode());
            save(relation);
            return true;
            //throw ServiceException.with(ServiceErrorEnum.DATA_EMPTY_ERROR);
        }
        //修改
        MemberRelation memberRelationForUpdate = new MemberRelation();
        memberRelationForUpdate.setUserCode(memberRelationDTO.getUserCode());
        memberRelationForUpdate.setId(memberRelation.getId());
        int update = memberRelationMapper.updateById(memberRelationForUpdate);
        return update > 0;
    }


    /**
     * 解绑资源
     *
     * @param memberRelationDTO
     * @date: 2022/5/13 14:15
     * @author wubaizhao1
     */
    @Override
    public Boolean unBinding(MemberRelationDTO memberRelationDTO) {
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(memberRelationDTO.getId(), "关系id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        MemberRelation removeEntity = new MemberRelation();
        removeEntity.setId(memberRelationDTO.getId());
        removeEntity.setYn(DataYnEnum.INVALID.getCode());
        int remove = memberRelationMapper.deleteById(removeEntity);
        return remove > 0;
    }

    /**
     * 查询某用户 下的某类型资源的id
     *
     * @param memberRelationDTO
     * @return
     * @date: 2022/5/13 14:18
     * @author wubaizhao1
     */
    @Override
    public List<Long> listResourceIds(MemberRelationDTO memberRelationDTO) {
        log.info("MemberRelationServiceImpl listResourceIds param={}", JsonUtils.toJSONString(memberRelationDTO));
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper();
        lqw.like(EmptyUtil.isNotEmpty(memberRelationDTO.getUserCode()), MemberRelation::getUserCode, memberRelationDTO.getUserCode());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceType()), MemberRelation::getResourceType, memberRelationDTO.getResourceType());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceRole()), MemberRelation::getResourceRole, memberRelationDTO.getResourceRole());
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        if (EmptyUtil.isNotEmpty(memberRelationDTO.getResourceRoleList())) {
            lqw.in(MemberRelation::getResourceRole, memberRelationDTO.getResourceRoleList());
        }
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceId()), MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        List<MemberRelation> memberRelations = memberRelationMapper.selectList(lqw);
        log.info("MemberRelationServiceImpl listResourceIds result={}", JsonUtils.toJSONString(memberRelations));
        List<Long> ids = memberRelations.stream().map(x -> x.getResourceId()).collect(Collectors.toList());
        return ids;
    }

    /**
     * 某资源下的各个成员id
     *
     * @param memberRelationDTO
     * @return
     */
    @Override
    public List<String> listUserCodeByResource(MemberRelationDTO memberRelationDTO) {
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(memberRelationDTO.getResourceId(), "资源id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceType(), "资源类型不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        if (EmptyUtil.isAllEmpty(memberRelationDTO.getResourceRole(), memberRelationDTO.getResourceRoleList())) {
            throw ServiceException.with(ServiceErrorEnum.SERVICE_INVALID_PARAMETER, "资源角色不能为空");
        }
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper();
        //except userId;
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceType()), MemberRelation::getResourceType, memberRelationDTO.getResourceType());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceRole()), MemberRelation::getResourceRole, memberRelationDTO.getResourceRole());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceId()), MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        if (EmptyUtil.isNotEmpty(memberRelationDTO.getResourceRoleList())) {
            lqw.in(MemberRelation::getResourceRole, memberRelationDTO.getResourceRoleList());
        }
        List<MemberRelation> memberRelations = memberRelationMapper.selectList(lqw);
        //UserIds
        List<String> userCodeList = memberRelations.stream().map(x -> x.getUserCode()).collect(Collectors.toList());
        return userCodeList;
    }

    @Override
    public List<MemberRelationWithUser> listRelationWithUserInfoByResource(MemberRelationDTO memberRelationDTO) {
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(memberRelationDTO.getResourceId(), "资源id不能为空", ServiceErrorEnum.INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceType(), "资源类型不能为空", ServiceErrorEnum.INVALID_PARAMETER.getCode());
        if (EmptyUtil.isAllEmpty(memberRelationDTO.getResourceRole(), memberRelationDTO.getResourceRoleList())) {
            throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "资源角色不能为空");
        }
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper();
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceType()), MemberRelation::getResourceType, memberRelationDTO.getResourceType());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceRole()), MemberRelation::getResourceRole, memberRelationDTO.getResourceRole());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceId()), MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        if (EmptyUtil.isNotEmpty(memberRelationDTO.getResourceRoleList())) {
            lqw.in(MemberRelation::getResourceRole, memberRelationDTO.getResourceRoleList());
        }
        List<MemberRelation> memberRelations = memberRelationMapper.selectList(lqw);
        List<MemberRelationWithUser> withUserList = new ArrayList<>();
        if (EmptyUtil.isNotEmpty(memberRelations)) {
            List<String> userCodes = memberRelations.stream().map(item->item.getUserCode()).collect(Collectors.toList());
            List<UserInfo> userInfos = userInfoService.getUsers(userCodes);
             Map<String, List<UserInfo>> userCode2Users = userInfos.stream().collect(Collectors.groupingBy(UserInfo::getUserCode));
            for (MemberRelation memberRelation : memberRelations) {
                String userCode = memberRelation.getUserCode();
                UserInfo userInfo = null;
                if(userCode2Users.get(userCode)!=null){
                    userInfo = userCode2Users.get(userCode).get(0);
                }


                MemberRelationWithUser memberRelationWithUser = new MemberRelationWithUser();
                BeanUtils.copyProperties(memberRelation, memberRelationWithUser);
                if (userInfo != null) {
                    memberRelationWithUser.setDept(userInfo.getDept());
                    memberRelationWithUser.setUserName(userInfo.getUserName());
                    memberRelationWithUser.setUserCode(maskPinHelper.maskUserCode(userInfo.getUserCode()));
                    memberRelationWithUser.setLoginType(userInfo.getLoginType());

                }else{
                    memberRelation.setUserCode(memberRelation.getUserCode());
                }
                withUserList.add(memberRelationWithUser);
            }
        }
        return withUserList;
    }

    /**
     * 检查是否是租户管理员
     *
     * @param userCode
     * @return
     */
    public Boolean checkTenantAdmin(String userCode) {
        if(userCode == null) return false;
        Guard.notEmpty(userCode, "用户Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        //是否存在租户管理员,如果不存在,则直接返回false,减少sql压力
        if (!checkTenantAdminFlag) {
            return Boolean.FALSE;
        }
        LambdaQueryWrapper<MemberRelation> lqw_tenant = new LambdaQueryWrapper();
        lqw_tenant.eq(MemberRelation::getUserCode, userCode);
        lqw_tenant.eq(MemberRelation::getResourceType, ResourceTypeEnum.TENANT_ADMIN.getCode());
        lqw_tenant.eq(MemberRelation::getResourceId, DEFAULT);
        lqw_tenant.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        MemberRelation memberRelation = memberRelationMapper.selectOne(lqw_tenant);
        if (memberRelation != null) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 查看资源的负责人
     *
     * @param memberRelationDTO
     * @return
     * @date: 2022/5/31 16:38
     * @author wubaizhao1
     */
    @Override
    public UserInfo getAdminWithUser(MemberRelationDTO memberRelationDTO) {
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        //Guard.notEmpty(memberRelationDTO.getUserCode(),"用户Code不能为空",ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceId(), "资源id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceType(), "资源类型不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper();
        lqw.eq(MemberRelation::getResourceType, memberRelationDTO.getResourceType());
        lqw.eq(MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        lqw.eq(MemberRelation::getResourceRole, ResourceRoleEnum.ADMIN.getCode());
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        List<MemberRelation> memberRelations = memberRelationMapper.selectList(lqw);
        MemberRelation memberRelation = null;
        if(!memberRelations.isEmpty()){
            memberRelation = memberRelations.get(0);
        }
        if (memberRelation == null) {
            return null;
        }
        String userCode = memberRelation.getUserCode();
        UserInfo userInfo = userInfoService.getOne(userCode);
        return userInfo;
    }

    @Override
    public ResourceRoleEnum getRole(MemberRelationDTO memberRelationDTO) {
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(memberRelationDTO.getUserCode(), "用户Code不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceId(), "资源id不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceType(), "资源类型不能为空", ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getCode());
        //1.先检查是否为租户管理员
        LambdaQueryWrapper<MemberRelation> lqw_tenant = new LambdaQueryWrapper();
        lqw_tenant.eq(MemberRelation::getUserCode, memberRelationDTO.getUserCode());
        lqw_tenant.eq(MemberRelation::getResourceType, ResourceTypeEnum.TENANT_ADMIN.getCode());
        lqw_tenant.eq(MemberRelation::getResourceId, DEFAULT);
        lqw_tenant.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        MemberRelation memberRelation = memberRelationMapper.selectOne(lqw_tenant);
        if (memberRelation != null) {
            return ResourceRoleEnum.TENANT_ADMIN;
        }
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper();
        lqw.eq(MemberRelation::getUserCode, memberRelationDTO.getUserCode());
        lqw.eq(MemberRelation::getResourceType, memberRelationDTO.getResourceType());
        lqw.eq(MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        memberRelation = memberRelationMapper.selectOne(lqw);
        if (memberRelation != null && memberRelation.getResourceRole() != null) {
            return ResourceRoleEnum.getByCode(memberRelation.getResourceRole());
        }
        return null;
    }

    public void fixInterfaceAdminInfo(List<InterfaceManage> interfaceManages, Integer interfaceType) {
        if (interfaceManages.isEmpty()) return;
        LambdaQueryWrapper<MemberRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemberRelation::getResourceType, interfaceType);
        wrapper.eq(MemberRelation::getResourceRole, ResourceRoleEnum.ADMIN.getCode());
        final List<Long> interfaceIds = interfaceManages.stream().map(vs -> vs.getId()).collect(Collectors.toList());
        List<Long> appIds = interfaceManages.stream().filter(vs -> vs.getAppId() != null).map(vs -> vs.getAppId()).collect(Collectors.toList());
        Map<Long, List<AppInfo>> appId2Apps = new HashMap<>();
        if (!appIds.isEmpty()) {
            List<AppInfo> apps = appInfoService.listByIds(appIds);
            appId2Apps = apps.stream().collect(Collectors.groupingBy(AppInfo::getId));
        }
        wrapper.in(MemberRelation::getResourceId, interfaceIds);
        List<MemberRelation> relations = list(wrapper);
        log.info("member.get_interface_relations:interfaceIds={},relations={}", interfaceIds, relations.stream().map(vs -> vs.getId()).collect(Collectors.toList()));
        //fixRelationUsers(relations);
        Map<Long, List<MemberRelation>> interfaceId2Relations = relations.stream().collect(Collectors.groupingBy(MemberRelation::getResourceId));
        for (InterfaceManage interfaceManage : interfaceManages) {
            List<MemberRelation> interfaceRelation = interfaceId2Relations.get(interfaceManage.getId());
            if (interfaceRelation != null) {
                interfaceManage.setUserCode(interfaceRelation.get(0).getUserCode());
                interfaceManage.setUserName(interfaceRelation.get(0).getUserName());
            } else {
                List<AppInfo> appInfos = appId2Apps.get(interfaceManage.getAppId());
                if (appInfos != null) {
                    AppInfoDTO dto = new AppInfoDTO();
                    dto.splitMembers(appInfos.get(0).getMembers());
                    if (dto.getOwner() != null && !dto.getOwner().isEmpty()) {
                        interfaceManage.setUserCode(dto.getOwner().get(0));
                    }


                }
            }
        }
        fixInterfaceUserName(interfaceManages);
    }

    private void fixInterfaceUserName(List<InterfaceManage> interfaceManages) {
        List<String> userCodes = interfaceManages.stream().filter(vs -> !StringUtils.isEmpty(vs.getUserCode())).map(vs -> vs.getUserCode()).collect(Collectors.toList());
        if (ObjectHelper.isEmpty(userCodes)) return;
        List<UserInfo> userInfos = userInfoService.getUsers(userCodes);
        log.info("user.getUser_code:userCodes={},userInfos={}", userCodes, userInfos.stream().map(vs -> vs.getId()).collect(Collectors.toList()));
        final Map<String, List<UserInfo>> userMap = userInfos.stream().collect(Collectors.groupingBy(UserInfo::getUserCode));
        log.info("user.get_user_map={}", userMap);
        for (InterfaceManage manage : interfaceManages) {
            manage.setUserName(manage.getUserCode());
            List<UserInfo> users = userMap.get(manage.getUserCode());
            if (users != null) {
                manage.setUserName(users.get(0).getUserName());
            }
        }
    }

    public void fixRelationUsers(List<MemberRelation> relations) {
        if (relations.isEmpty()) return;
        List<String> userCodes = relations.stream().map(vs -> vs.getUserCode()).collect(Collectors.toList());
        List<UserInfo> userInfos = userInfoService.getUsers(userCodes);
        log.info("user.getUser_code:userCodes={},userInfos={}", userCodes, userInfos.stream().map(vs -> vs.getId()).collect(Collectors.toList()));
        final Map<String, List<UserInfo>> userMap = userInfos.stream().collect(Collectors.groupingBy(UserInfo::getUserCode));
        log.info("user.get_user_map={}", userMap);
        for (MemberRelation relation : relations) {
            relation.setUserName(relation.getUserCode());
            final List<UserInfo> users = userMap.get(relation.getUserCode());
            if (users != null) {
                relation.setUserName(users.get(0).getUserName());
                relation.setDeptName(users.get(0).getDept());
            }
        }
    }

    /**
     * 校验用户资源权限
     *
     * @param memberRelationDTO
     * @return true 有权限   false 无权限
     */
    public boolean checkResourceAuth(MemberRelationDTO memberRelationDTO) {
        // 查询是否已存在 [逻辑唯一索引]:  用户Code 资源id 资源类型
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode())
                .eq(MemberRelation::getUserCode, memberRelationDTO.getUserCode())
                .eq(MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        int count = memberRelationMapper.selectCount(lqw);
        if (count > 0) {
            // 用户有资源权限
            return true;
        }
        // 用户无资源权限
        return false;
    }

    @Override
    public List<MemberRelation> listByInterfaceId(Long interfaceId) {
        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper<>();
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode())
                .eq(MemberRelation::getResourceType, ResourceTypeEnum.INTERFACE.getCode())
                .eq(MemberRelation::getResourceId, interfaceId);
        return list(lqw);
    }

    @Override
    public Page<MemberRelationWithUser> pageListRelationWithUserInfoByResource(MemberRelationDTO memberRelationDTO) {
        Guard.notEmpty(memberRelationDTO, ServiceErrorEnum.SERVICE_INVALID_PARAMETER.getMsg());
        Guard.notEmpty(memberRelationDTO.getResourceId(), "资源id不能为空", ServiceErrorEnum.INVALID_PARAMETER.getCode());
        Guard.notEmpty(memberRelationDTO.getResourceType(), "资源类型不能为空", ServiceErrorEnum.INVALID_PARAMETER.getCode());
        if (EmptyUtil.isAllEmpty(memberRelationDTO.getResourceRole(), memberRelationDTO.getResourceRoleList())) {
            throw ServiceException.with(ServiceErrorEnum.INVALID_PARAMETER, "资源角色不能为空");
        }

        LambdaQueryWrapper<MemberRelation> lqw = new LambdaQueryWrapper();
        lqw.eq(MemberRelation::getYn, DataYnEnum.VALID.getCode());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceType()), MemberRelation::getResourceType, memberRelationDTO.getResourceType());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceRole()), MemberRelation::getResourceRole, memberRelationDTO.getResourceRole());
        lqw.eq(EmptyUtil.isNotEmpty(memberRelationDTO.getResourceId()), MemberRelation::getResourceId, memberRelationDTO.getResourceId());
        if (EmptyUtil.isNotEmpty(memberRelationDTO.getResourceRoleList())) {
            lqw.in(MemberRelation::getResourceRole, memberRelationDTO.getResourceRoleList());
        }
//        List<MemberRelation> memberRelations = memberRelationMapper.selectList(lqw);
        Page<MemberRelation> memberRelationPage=  page(new Page<>(memberRelationDTO.getCurrent(),memberRelationDTO.getSize()),lqw);

        List<MemberRelationWithUser> withUserList = new ArrayList<>();
        Page<MemberRelationWithUser> memberRelationWithUserPage= new Page<>(memberRelationDTO.getCurrent(),memberRelationDTO.getSize());
        memberRelationWithUserPage.setTotal(memberRelationPage.getTotal());
        memberRelationWithUserPage.setRecords(withUserList);
        if (EmptyUtil.isNotEmpty(memberRelationPage.getRecords())) {
            List<String> userCodes = memberRelationPage.getRecords().stream().map(item->item.getUserCode()).collect(Collectors.toList());
            List<UserInfo> userInfos = userInfoService.getUsers(userCodes);
            Map<String, List<UserInfo>> userCode2Users = userInfos.stream().collect(Collectors.groupingBy(UserInfo::getUserCode));
            for (MemberRelation memberRelation : memberRelationPage.getRecords()) {
                String userCode = memberRelation.getUserCode();
                UserInfo userInfo = null;
                if(userCode2Users.get(userCode)!=null){
                    userInfo = userCode2Users.get(userCode).get(0);
                }


                MemberRelationWithUser memberRelationWithUser = new MemberRelationWithUser();
                BeanUtils.copyProperties(memberRelation, memberRelationWithUser);
                if (userInfo != null) {
                    memberRelationWithUser.setDept(userInfo.getDept());
                    memberRelationWithUser.setUserName(userInfo.getUserName());
                    memberRelationWithUser.setUserCode(maskPinHelper.maskUserCode(userInfo.getUserCode()));
                    memberRelationWithUser.setLoginType(userInfo.getLoginType());

                }else{
                    memberRelation.setUserCode(memberRelation.getUserCode());
                }
                withUserList.add(memberRelationWithUser);
            }
        }
        return memberRelationWithUserPage;
    }

}
