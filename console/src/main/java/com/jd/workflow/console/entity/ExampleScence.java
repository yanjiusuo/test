package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 方法示例
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "method_example_scence")
public class ExampleScence extends BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;

	/**
	 * 场景名称编码- 英文名称
	 */
	private String scenceName;
	/**
	 * 方法id
	 */
	private Long methodId;
	/**
	 * 请求内容
	 */
	private String inputExample;
	/**
	 * @hidden
	 */
	private String outputExample;

	private String description;








}
