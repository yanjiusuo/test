package com.jd.workflow.console.service.role;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/29
 */

import com.jd.cjg.acc.client.entity.param.query.RoleQueryParam;
import com.jd.cjg.acc.client.entity.param.query.UserRoleQueryParam;
import com.jd.cjg.acc.client.entity.po.RoleInfo;
import com.jd.cjg.acc.client.service.AccRoleService;
import com.jd.cjg.acc.client.service.AccUserService;
import com.jd.cjg.acc.client.utils.AccServiceUtil;
import com.jd.official.omdm.is.hr.vo.UserVo;
import com.jd.workflow.console.base.UserSessionLocal;
import com.jd.workflow.console.dto.role.QueryRoleParam;
import com.jd.workflow.console.dto.role.Role;
import com.jd.workflow.console.dto.role.UserRoleDTO;
import com.jd.workflow.console.helper.UserHelper;
import com.jd.workflow.soap.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/29
 */
@Slf4j
@Service
public class AccRoleServiceAdapterImpl implements AccRoleServiceAdapter {

    @Value("${acc.appId:up-portal}")
    private String appId;

    @Value("${acc.channelId:10}")
    private Long channelId;
    @Value("${acc.tenantId:26001}")
    private Long tenantId;

    @Resource
    private AccRoleService accRoleService;

    @Autowired
    private UserHelper userHelper;

    @Override
    public List<Role> queryRoleList(String operator, QueryRoleParam queryRoleParam) {
        if(Objects.isNull(queryRoleParam.getTenantId())

        ){
            queryRoleParam.setTenantId(UserSessionLocal.getUser().getTenantKey());
        }
        if (Objects.isNull(queryRoleParam.getTenantId())) {

            queryRoleParam.setTenantId(tenantId);
        }
        List<RoleInfo> roleInfoList = AccServiceUtil.callServiceCustomize(operator, appId, () -> {
            UserRoleQueryParam roleQueryParam = new UserRoleQueryParam();
            roleQueryParam.setChannelId(channelId);
            roleQueryParam.setTenantId(queryRoleParam.getTenantId());
            roleQueryParam.setRoleName(queryRoleParam.getRoleName());
            roleQueryParam.setUserName(operator);
            return roleQueryParam;
        }, accRoleService::queryUserAllRoles, result -> {
            log.error("RPC queryRoleList error,errCode={},errMsg={}", result.getErrCode(), result.getErrMsg());
            throw new BizException("RPC queryRoleList error");
        });

        return RoleConvertor.convertRpcRoleInfoListToRoleList(roleInfoList);
    }

    @Override
    public UserRoleDTO queryUserRole(String operator) {
        UserRoleDTO userRoleDTO = new UserRoleDTO();
        userRoleDTO.setJapiAdmin(false);
        userRoleDTO.setJapiDepartment(false);
        userRoleDTO.setDeptLeader(false);
        userRoleDTO.setTenantManager(false);
        userRoleDTO.setConsoleAdmin(false);


        QueryRoleParam queryRoleParam = new QueryRoleParam();
        List<Role> roleList = queryRoleList(operator, queryRoleParam);
        userRoleDTO.setRoleList(roleList);
        userRoleDTO.setTenantCode(UserSessionLocal.getUser().getTenantId());
        UserVo userVo = userHelper.getUserBaseInfoByUserName(operator);
        if (CollectionUtils.isNotEmpty(roleList)) {
            for (Role role : roleList) {
                if ("japi_department".equals(role.getRoleName())) {
                    userRoleDTO.setJapiDepartment(true);
                }
                if ("japi_admin".equals(role.getRoleName())) {
                    userRoleDTO.setJapiAdmin(true);
                }
                if ("admin".equals(role.getRoleName())) {
                    userRoleDTO.setConsoleAdmin(true);
                }
                if ("tenant_manager".equals(role.getRoleName())) {
                    userRoleDTO.setTenantManager(true);
                }
            }
        }
        if (userVo.getPositionName().equals("机构负责人岗")) {
            userRoleDTO.setDeptLeader(true);

        }
        userRoleDTO.setDept(userVo.getOrganizationFullName());
        userRoleDTO.setErp(operator);
        userRoleDTO.setUserName(userVo.getRealName());

        return userRoleDTO;
    }



    static class RoleConvertor {
        public static Role convertRpcRoleInfoToRole(RoleInfo rpcRoleInfo) {
            Role role = new Role();
            BeanUtils.copyProperties(rpcRoleInfo, role);
            role.setCreateUser(rpcRoleInfo.getCreateBy());
            Date createAt = rpcRoleInfo.getCreateAt();
            role.setCreateTime(createAt != null ? new Timestamp(rpcRoleInfo.getCreateAt().getTime()) : null);
            return role;
        }

        public static List<Role> convertRpcRoleInfoListToRoleList(List<RoleInfo> rpcRoleInfoList) {
            if (CollectionUtils.isEmpty(rpcRoleInfoList)) {
                log.error("rpcRoleInfoList is null");
                return Collections.emptyList();
            }
            return rpcRoleInfoList.stream()
                    .map(RoleConvertor::convertRpcRoleInfoToRole)
                    .collect(Collectors.toList());
        }
    }
}
