package com.jd.workflow.console.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.base.PageParam;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoDTO extends PageParam {
	/**
	 * 主键
	 */
	private Long id;

	/**
	 * 用户部门
	 */
	private String dept;

	private String password;

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
	 * 模糊搜索的key
	 * @date: 2022/6/21 10:58
	 * @author wubaizhao1
	 */
	private String key;
//	/**
//	 * 租户id
//	 */
//	private Long tenantId;

//	/**
//	 * 逻辑删除标示 0、删除 1、有效
//	 */
//	private Integer yn;
//
//	/**
//	 * 创建者
//	 */
//	private String creator;
//
//	/**
//	 * 修改者
//	 */
//	private String modifier;
//
//	/**
//	 * 创建时间
//	 */
//	private LocalDateTime created;
//
//	/**
//	 * 修改时间
//	 */
//	private LocalDateTime modified;


}
