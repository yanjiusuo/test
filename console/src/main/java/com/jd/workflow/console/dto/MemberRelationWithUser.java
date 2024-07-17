package com.jd.workflow.console.dto;

import lombok.Data;

@Data
public class MemberRelationWithUser {
	/**
	 * 主键
	 */
	private Long id;

	/**
	 * 用户id
	 */
	private Long userId;

	/**
	 * 资源id
	 */
	private Long resourceId;

	/**
	 * 根据枚举控制
	 */
	private Integer resourceType;

	/**
	 * 资源角色 0-无 1-租户管理员 2负责人 3成员
	 */
	private Integer resourceRole;

//	/**
//	 * 租户id
//	 */
//	private Long tenantId;


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
}
