package com.jd.workflow.console.service.role;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/29
 */

import com.jd.workflow.console.dto.role.QueryRoleParam;
import com.jd.workflow.console.dto.role.Role;
import com.jd.workflow.console.dto.role.UserRoleDTO;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/29 
 */
public interface AccRoleServiceAdapter {
    List<Role> queryRoleList(String operator, QueryRoleParam queryRoleParam);

    UserRoleDTO queryUserRole(String operator);
}
