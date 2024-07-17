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
public enum RankTypeEnum {
	INTERFACE(1,"接口分组"),
	METHOD(2,"方法分组")
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
	public static RankTypeEnum getByCode(Integer code){
		for (RankTypeEnum value : RankTypeEnum.values()) {
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
	public static RankTypeEnum getByName(String typeName){
		String name=typeName.trim().toLowerCase();
		for (RankTypeEnum value : RankTypeEnum.values()) {
			if(value.getDesc().equals(name)){
				return value;
			}
		}
		return null;
	}
}
