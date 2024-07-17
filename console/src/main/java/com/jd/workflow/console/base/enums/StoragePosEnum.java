package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum StoragePosEnum {
	DATA_BASE(1,"数据库"),
	OSS(2,"对象存储"),
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

	public static StoragePosEnum getByCode(Integer code){
		for (StoragePosEnum value : StoragePosEnum.values()) {
			if(value.getCode().equals(code)){
				return value;
			}
		}
		return null;
	}
}
