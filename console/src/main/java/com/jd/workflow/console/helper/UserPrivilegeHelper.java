package com.jd.workflow.console.helper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.jd.workflow.console.base.enums.DataYnEnum;
import com.jd.workflow.console.base.enums.ResourceRoleEnum;
import com.jd.workflow.console.base.enums.ResourceTypeEnum;
import com.jd.workflow.console.dao.mapper.MemberRelationMapper;
import com.jd.workflow.console.dto.MemberRelationDTO;
import com.jd.workflow.console.entity.*;
import com.jd.workflow.console.service.*;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 项目名称：权限认证
 * 类 名 称：UserPrivilegeHelper
 * 类 描 述：权限校验
 * 创建时间：2022-05-25 09:54
 * 创 建 人：wangxiaofei8
 */
@Slf4j
@Service
public class UserPrivilegeHelper {

    @Resource
    private MemberRelationMapper memberRelationMapper;

    @Resource
    IMemberRelationService memberRelationService;

    @Resource
    IMethodManageService methodManageService;
    @Autowired
    IAppInfoService appInfoService;

    @Resource
    IInterfaceManageService interfaceManageService;
    @Autowired
    IAppInfoMembersService appInfoMembersService;

    @Resource
    IFlowParamGroupService flowParamGroupService;
//    public boolean hasPrivilege(String userCode,String signature){
//        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
//        memberRelationDTO.setResourceType(ResourceTypeEnum.INTERFACE.getCode());
//        memberRelationDTO.setResourceId(Long.valueOf(signature));
//        memberRelationDTO.setUserCode(userCode);
//        ResourceRoleEnum role = memberRelationService.getRole(memberRelationDTO);
//
//        if(role==null){
//            log.warn("userCode={} , signature={} hasPrivilege return false >>>>>>>>>",userCode,signature);
//            return false;
//        }
//        if( role==ResourceRoleEnum.TENANT_ADMIN || role==ResourceRoleEnum.ADMIN ){
//            return true;
//        }else {
//            return false;
//        }
//    }

    /**
     * 用于id为关系id 直接删除关系id的
     *
     * @param id
     * @param userCode
     * @return
     */
    public boolean hasPrivilegeByRelationId(long id, String userCode) {
        if (memberRelationService.checkTenantAdmin(userCode)) {
            return true;
        }
        MemberRelation memberRelation = memberRelationService.getById(id);
        if (memberRelation == null) {
            return false;
        }
        if (Objects.equals(memberRelation.getResourceType(), ResourceTypeEnum.INTERFACE.getCode())
                || Objects.equals(memberRelation.getResourceType(), ResourceTypeEnum.ORCHESTRATION.getCode())) {
            Long interfaceId = memberRelation.getResourceId();
            Integer resourceRole = memberRelation.getResourceRole();
            //被删除人的权限，如果被删除人的权限大于删除人，则不允许删除
            ResourceRoleEnum edRole = ResourceRoleEnum.getByCode(resourceRole);
            InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
            // 操作人的权限
            ResourceRoleEnum role = null;
            if (hasAppRole(interfaceManage.getAppId(), userCode)) {
                role = ResourceRoleEnum.TENANT_ADMIN;
            } else {
                role = getRoleByInterface(interfaceId, userCode);
            }
            if (role == null || edRole == null) {
                return false;
            }
            return checkRoleType(edRole, role);
        }
        if (Objects.equals(memberRelation.getResourceType(), ResourceTypeEnum.PARAM_GROUP.getCode())) {
            Long resourceId = memberRelation.getResourceId();
            Integer resourceRole = memberRelation.getResourceRole();
            //被删除人的权限，如果被删除人的权限大于删除人，则不允许删除
            ResourceRoleEnum edRole = ResourceRoleEnum.getByCode(resourceRole);
            FlowParamGroup flowParamGroup = flowParamGroupService.getGroupByGroupId(resourceId);
            if (Objects.isNull(flowParamGroup)) {
                return false;
            }
            // 操作人的权限
            ResourceRoleEnum role = getRoleByParamGroup(resourceId, userCode);
            if (Objects.isNull(edRole) || Objects.isNull(role)) {
                return false;
            }
            return checkRoleType(edRole, role);
        }
        return false;
    }

    private boolean checkRoleType(ResourceRoleEnum edRole, ResourceRoleEnum role) {
        switch (edRole) {
            case TENANT_ADMIN:
                return role == ResourceRoleEnum.TENANT_ADMIN;
            case ADMIN:
                switch (role) {
                    case TENANT_ADMIN:
                    case ADMIN:
                        return true;
                    case MEMBER:
                    case DEFAULT:
                    default:
                        return false;
                }
            case MEMBER:
                switch (role) {
                    case TENANT_ADMIN:
                    case ADMIN:
                    case MEMBER:
                        return true;
                    case DEFAULT:
                    default:
                        return false;
                }
            case DEFAULT:
            default:
                return true;
        }
    }

    /**
     * 用于id为方法id 删除方法、调试方法鉴权使用
     *
     * @param methodId
     * @param userCode
     * @return
     */
    public boolean hasPrivilegeByMethodId(long methodId, String userCode) {
        if (memberRelationService.checkTenantAdmin(userCode)) {
            return true;
        }
        MethodManage methodManage = methodManageService.getById(methodId);
        if (methodManage == null) {
            return false;
        }
        Long interfaceId = methodManage.getInterfaceId();
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        if (Objects.isNull(interfaceManage)) {
            return false;
        }
        //japi demo，所有人都有权限操作
        if (interfaceManage.getAppId().equals(19537L)) {
            return true;
        }
        return hasInterfaceRole(interfaceId, userCode);
    }

    public boolean hasAppRole(Long appId, String userCode) {
        if (appId == null) return false;
        AppInfo app = appInfoService.getById(appId);
        if (app == null) return false;
        return appInfoMembersService.getMemberByErp(userCode, appId) != null;
    }

    public boolean hasAppRoleByInterface(Long interfaceId, String userId) {
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        return hasAppRole(interfaceManage.getAppId(), userId);
    }

    public Boolean hasInterfaceRole(Long interfaceId, String userCode) {
        if (memberRelationService.checkTenantAdmin(userCode)) {
            return true;
        }
        ResourceRoleEnum role = getRoleByInterface(interfaceId, userCode);
        if (role == null) {
            log.warn("userCode={} , interfaceId={} getInterfaceRole return false >>>>>>>>>", userCode, interfaceId);
            return hasAppRoleByInterface(interfaceId, userCode);
        }
        if (role == ResourceRoleEnum.TENANT_ADMIN || role == ResourceRoleEnum.ADMIN || role == ResourceRoleEnum.MEMBER) {
            return true;
        } else {
            return hasAppRoleByInterface(interfaceId, userCode);
        }
    }

    private ResourceRoleEnum getRoleByInterface(Long interfaceId, String userCode) {
        InterfaceManage interfaceManage = interfaceManageService.getById(interfaceId);
        if (interfaceManage == null) {
            return null;
        }
        //japi demo应用所有人都有权限
        if (interfaceManage.getAppId().equals(19537L)) {
            return ResourceRoleEnum.MEMBER;
        }
        ResourceTypeEnum resourceType = ResourceTypeEnum.getResourceType(interfaceManage.getType());
        if (resourceType == null) {
            return null;
        }
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(resourceType.getCode());
        memberRelationDTO.setResourceId(interfaceId);
        memberRelationDTO.setUserCode(userCode);
        ResourceRoleEnum role = memberRelationService.getRole(memberRelationDTO);
        return role;
    }


    private ResourceRoleEnum getRoleByParamGroup(Long groupId, String userCode) {
        MemberRelationDTO memberRelationDTO = new MemberRelationDTO();
        memberRelationDTO.setResourceType(ResourceTypeEnum.PARAM_GROUP.getCode());
        memberRelationDTO.setResourceId(groupId);
        memberRelationDTO.setUserCode(userCode);
        ResourceRoleEnum role = memberRelationService.getRole(memberRelationDTO);
        return role;
    }
}
