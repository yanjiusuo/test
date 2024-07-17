package com.jd.workflow.console.dto.role;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/29
 */

import lombok.Data;
import titan.profiler.shade.com.google.api.BackendOrBuilder;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/1/29
 */
@Data
public class UserRoleDTO {


    /**
     * 租户管理员
     */
    private boolean tenantManager;
    /**
     * 平台管理员
     */
    private Boolean consoleAdmin;
    /**
     * japi超级管理员
     */
    private Boolean japiAdmin;

    /**
     * japi部门接口人
     */
    private Boolean japiDepartment;

    /**
     * 机构负责人
     */
    private Boolean deptLeader;

    /**
     * 部门
     */
    private String dept;

    /**
     * 用户名
     */
    private String userName;

    /**
     * erp
     */
    private String erp;

    /**
     * 用户角色
     */
    private List<Role> roleList;

    /***
     * 租户编码
     */
    private String tenantCode;
}
