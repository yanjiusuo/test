package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum LoginTypeEnum {
	DEFAULT(128),
	ERP(0),
	PIN(1),
	PHONE(2),
	HEALTH(3),
	/**
	 * 自己处理登录体系
	 */
	SELF(4)
	;

	/**
	 *
	 */
	@Getter
	@Setter
	private Integer code;
}
