package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 资源角色 0-无 1-租户管理员 2负责人 3成员
 * @date: 2022/5/12 18:29
 * @author wubaizhao1
 */
@AllArgsConstructor
public enum ResourceRoleEnum {
	DEFAULT(0,"非成员"),
	TENANT_ADMIN(1,"租户管理员"),
	ADMIN(2,"负责人"),
	MEMBER(3,"成员"),
	READONLY_MEMBER(4,"只读成员")

	;
	/**
	 * @date: 2022/5/12 18:23
	 * @author wubaizhao1
	 */
	@Getter
	@Setter
	private Integer code;

	/**
	 * 描述
	 * @date: 2022/5/12 18:25
	 * @author wubaizhao1
	 */
	@Getter
	@Setter
	private String desc;

	public static ResourceRoleEnum getByCode(Integer code){
		for (ResourceRoleEnum value : ResourceRoleEnum.values()) {
			if(value.getCode().equals(code)){
				return value;
			}
		}
		return null;
	}
}
