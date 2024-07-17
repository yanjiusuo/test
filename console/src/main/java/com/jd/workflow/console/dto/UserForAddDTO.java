package com.jd.workflow.console.dto;

import lombok.Data;

@Data
public class UserForAddDTO {
    /**
     * 用户数据
     * --------------------------------
     */
    /**
     * 用户部门
     */
    private String dept;

    /**
     * 登录类型：0-erp 1-pin 2-手机号 3-健康体系
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

    /**
     * 是否已经存在
     * @date: 2022/6/1 16:46
     * @author wubaizhao1
     */
    private Boolean exist;
}
