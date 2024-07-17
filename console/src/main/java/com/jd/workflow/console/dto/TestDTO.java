package com.jd.workflow.console.dto;

import com.jd.workflow.console.base.PageParam;
import lombok.*;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestDTO extends PageParam {
	/**
	 * 主键
	 */
	private Long id;

	/**
	 * 用户部门
	 */
	private String dept;

	/**
	 * 嵌套类型
	 */
	private Deep1 deep1;

	/**
	 *
	 */
	private List<Deep2> deep2List;

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Deep1{
		Integer code;
		String desc;
	}

	@Data
	@AllArgsConstructor
	@NoArgsConstructor
	public static class Deep2{
		Integer code;
		String desc;
	}
}
