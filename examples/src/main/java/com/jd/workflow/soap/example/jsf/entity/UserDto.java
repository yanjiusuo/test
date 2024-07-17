package com.jd.workflow.soap.example.jsf.entity;

import lombok.Data;

@Data
public class UserDto {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */

    private Long id;

    /**
     * 用户部门
     */

    private String dept;

    /**
     * 登录类型：0-erp 1-pin 2-手机号 3-健康体系
     * link{@com.jd.workflow.console.base.enums.LoginTypeEnum}
     */

    private Integer loginType;

    /**
     * 用户编码（英文）
     */

    private String userCode;

    /**
     * 用户名称
     */

    private String userName;

    private String password;

}
