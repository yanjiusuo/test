package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 接口类型 0接口 1方法
 */
@AllArgsConstructor
public enum InterfaceSimpleTypeEnum {
	Interface(0,"interface"),
	method(1,"method"),

	;
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


	/**
	 * @date: 2022/5/19 15:55
	 * @author wubaizhao1
	 * @param code
	 * @return
	 */
	public static InterfaceSimpleTypeEnum getByCode(Integer code){
		for (InterfaceSimpleTypeEnum value : InterfaceSimpleTypeEnum.values()) {
			if(value.getCode().equals(code)){
				return value;
			}
		}
		return null;
	}

	/**
	 *
	 * @param typeName
	 * @return
	 */
	public static InterfaceSimpleTypeEnum getByName(String typeName){
		String name=typeName.trim().toLowerCase();
		for (InterfaceSimpleTypeEnum value : InterfaceSimpleTypeEnum.values()) {
			if(value.getDesc().equals(name)){
				return value;
			}
		}
		return null;
	}
}
