package com.jd.workflow.console.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.base.PageParam;
import lombok.Data;

import java.util.List;

@Data
public class MemberRelationDTO extends PageParam {
	/**
	 * 主键
	 */
	private Long id;

	/**
	 * 用户唯一标识
	 */
	private String userCode;

	/**
	 * 资源id
	 */
	private Long resourceId;

	/**
	 * 根据枚举控制
	 * {@link com.jd.workflow.console.base.enums.ResourceTypeEnum}
	 */
	private Integer resourceType;

	/**
	 * 资源角色 0-无 1-租户管理员 2负责人 3成员
	 * {@link com.jd.workflow.console.base.enums.ResourceRoleEnum}
	 */
	private Integer resourceRole;

	/**
	 * 角色列表
	 * @date: 2022/5/18 11:13
	 * @author wubaizhao1
	 */
	private List<Integer> resourceRoleList;
}
