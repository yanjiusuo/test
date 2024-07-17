package com.jd.workflow.console.dto.test.deeptest;

import lombok.Data;

@Data
public class ModuleUserInfo {

    /**
     * 主键id
     */
    private Long id;

    /**
     * 模块id
     */
    private Long catalogId;

    /**
     * 范围：部门维度，用户维度
     */
    private Integer scope;

    /**
     * 用户id
     */
    private Long memberId;

    /**
     * 用户详情信息
     */
    private MemberUserInfo memberInfo;

    /**
     * 部门名称
     */
    private String departName;

    /**
     * 部门全称
     */
    private String deptFullName;

    /**
     * 角色id，0：管理员，1：成员
     */
    private Integer roleId;
}
