package com.jd.workflow.console.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.jd.workflow.console.base.PageParam;
import com.jd.workflow.console.dto.doc.DocConfigDto;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Data
public class InterfaceManageDTO extends PageParam {
	/**
	 * 主键
	 */
	private Long id;

	/**
	 * 接口类型 1-http、2-webservice、3-jsf 10-编排
	 * 默认把编排的隔离开,需要加筛选条件
	 */
	@NotNull(message = "类型不可为空")
	private Integer type;

	/**
	 * 接口名称
	 */
	private String name;

	/**
	 * 接口描述
	 */
	private String desc;

	/**
	 * 是否设置为demo   1-是  2-否
	 */
	private Integer isPublic;

	/**
	 * 是否有操作权限（不存入表中）  1-有  2-无
	 */
	private Integer hasAuth;

	/**
	 * 大json串
	 * {@link EnvModel}
	 * List<EnvModel>
	 */
	private String env;

	/**
	 * 租户id
	 */
	private String tenantId;

	/**
	 * 负责人code
	 */
	private String adminCode;

	/**
	 * 用户Code
	 */
	private String userCode;

	/**
	 * 用户名称
	 */
	private String userName;

	/**
	 * 地址
	 * @date: 2022/5/17 14:44
	 * @author wubaizhao1
	 */
	private String path;

	/**
	 * 编排类型 0-默认 1-单节点 2-多节点
	 * @date: 2022/6/1 16:03
	 * @author wubaizhao1
	 */
	private Integer nodeType;

	/**
	 * 服务编码
	 * @date: 2022/6/2 15:56
	 * @author wubaizhao1
	 */
	@NotNull(message = "服务编码不可为空")
	private String serviceCode;

	/**
	 * 环境 输出的时候赋值
	 * List<EnvModel>
	 */
	private List<EnvModel> envList;

	private String groupId;
	private String artifactId;
	private String version;

	private Long appId;

	String docInfo;
	/**
	 * 文档配置，编辑的时候使用
	 */
	Map<String,Object> config;
	DocConfigDto docConfig;
	/**
	 * 接口分级
	 */
	private Integer level;
	/**
	 * 接口可见性 0默认全部可见 1 应用成员可见
	 */
	private Integer visibility;

	/**
	 * 云文档链接
	 */
	private String cloudFilePath;
	/**
	 * 云文档标签
	 */
	private String cloudFileTags;

}
