package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
@AllArgsConstructor
public enum ResourceTypeEnum {
	INTERFACE(1,"接口"),

	TENANT_ADMIN(4,"租户管理员"),
	ORCHESTRATION(10,"编排"),
	PIN_MANAGE(11,"PIN管理"),
	PARAM_GROUP(12,"公共参数分组"),
	PRD_GROUP(20,"需求分组"),
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

	public static ResourceTypeEnum getResourceType(Integer interfaceType){
		InterfaceTypeEnum type = InterfaceTypeEnum.getByCode(interfaceType);
		ResourceTypeEnum resourceType=null;
		if(type==null){
			return null;
		}
		if(InterfaceTypeEnum.ORCHESTRATION.equals(type)){
			resourceType=ResourceTypeEnum.ORCHESTRATION;
		}else if(type != null){
			resourceType=ResourceTypeEnum.INTERFACE;
		}

		return resourceType;
	}
}
