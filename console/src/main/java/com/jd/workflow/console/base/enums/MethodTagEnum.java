package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum MethodTagEnum {
	COLOR(1,"color网关接口"),
	NORMAL(0,"普通接口"),
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

	public static MethodTagEnum getByCode(Integer code){
		for (MethodTagEnum value : MethodTagEnum.values()) {
			if(value.getCode().equals(code)){
				return value;
			}
		}
		return null;
	}
}
