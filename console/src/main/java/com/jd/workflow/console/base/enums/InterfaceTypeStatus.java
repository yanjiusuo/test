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
public enum InterfaceTypeStatus {
	ENABLED(1,"已上线"),
//	REPAIR(2,"维护"), // hidden
	ABANDON(3,"已废弃"),
//	PLAN(4,"规划中"), // hidden
	DEVELOPING(5,"开发中"),
	TEST(6,"测试中"),
//	BUG(7,"bug") // hidden
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


	/**
	 * @date: 2022/5/19 15:55
	 * @author wubaizhao1
	 * @param code
	 * @return
	 */
	public static InterfaceTypeStatus getByCode(Integer code){
		for (InterfaceTypeStatus value : InterfaceTypeStatus.values()) {
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
	public static InterfaceTypeStatus getByName(String typeName){
		String name=typeName.trim().toLowerCase();
		for (InterfaceTypeStatus value : InterfaceTypeStatus.values()) {
			if(value.getDesc().equals(name)){
				return value;
			}
		}
		return null;
	}
}
