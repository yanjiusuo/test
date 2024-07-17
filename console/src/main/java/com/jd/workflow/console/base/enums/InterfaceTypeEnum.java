package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 接口类型 1-http、2-webservice、3-jsf 10-编排
 * @date: 2022/5/12 18:23
 * @author wubaizhao1
 */
@AllArgsConstructor
public enum InterfaceTypeEnum {
	HTTP(1,"http"),
	WEB_SERVICE(2,"webservice"), 
		JSF(3,"jsf"),
	DUCC(4,"ducc"), 
	JIMDB(5,"jimdb"),
	BEAN(9,"bean"),
	//编排
	ORCHESTRATION(10,"orchestration"),
	DOC(20,"doc"),
	MODEL(21,"model"),
    //扩展点接口，本质也是一种http接口
	EXTENSION_POINT(22,"extensionPoint"),
	//接口市场搜索--按接口维度出数据
	JSF_INTERFACE(23,"jsfInterface"),
	;
	/**
	 * @date: 2022/5/12 18:23
	 * @author wubaizhao1 扩展点
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


	/**
	 * @date: 2022/5/19 15:55
	 * @author wubaizhao1
	 * @param code
	 * @return
	 */
	public static InterfaceTypeEnum getByCode(Integer code){
		for (InterfaceTypeEnum value : InterfaceTypeEnum.values()) {
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
	public static InterfaceTypeEnum getByName(String typeName){
		String name=typeName.trim().toLowerCase();
		for (InterfaceTypeEnum value : InterfaceTypeEnum.values()) {
			if(value.getDesc().equals(name)){
				return value;
			}
		}
		return null;
	}
}
