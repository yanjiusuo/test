package com.jd.workflow.console.dto.role;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/29
 */

import lombok.Data;

import java.io.Serializable;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/29 
 */
@Data
public class QueryRoleParam implements Serializable {
    private static final long serialVersionUID = -5374195937718644297L;
    /**
     * 角色名
     */
    private String roleName;

    /**
     * 租户ID
     */
    private Long tenantId;
}
