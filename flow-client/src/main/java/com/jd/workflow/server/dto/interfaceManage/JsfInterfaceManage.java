package com.jd.workflow.server.dto.interfaceManage;


import java.util.List;

public class JsfInterfaceManage{

	private Long appId;


	/**
	 * 接口类型 1-http、2-webservice、3-jsf 22-扩展点
	 */
	private Integer type;


	/**
	 * 快捷调用环境信息
	 */
	private List<JsfEnvModel> envInfo;

	/**
	 * 接口名称
	 */
	private String name;

	/**
	 * 接口描述
	 */
	private String desc;

	/**
	 * 服务编码 接口分组编码
	 * @date: 2022/6/2 15:56
	 * @author wubaizhao1
	 */
	private String serviceCode;

	/**
	 * 负责人code
	 */
	private String adminCode;

	/**
	 * artifactId jsf接口使用
	 */
	private String artifactId;
	/**
	 * 版本信息
	 */
	private String version;

	/**
	 * 文档信息
	 */
	String docInfo;
	/**
	 * 文档配置： "docConfig": {"docType": "md" }
	 */
	JsfDocConfigDto docConfig;
	/**
	 * 接口分级 0、1
	 */
	private Integer level;
	/**
	 * 接口可见性 0默认全部可见 1 应用成员可见
	 */
	private Integer visibility;

	private String tenantId;


	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public List<JsfEnvModel> getEnvInfo() {
		return envInfo;
	}

	public void setEnvInfo(List<JsfEnvModel> envInfo) {
		this.envInfo = envInfo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getServiceCode() {
		return serviceCode;
	}

	public void setServiceCode(String serviceCode) {
		this.serviceCode = serviceCode;
	}

	public String getAdminCode() {
		return adminCode;
	}

	public void setAdminCode(String adminCode) {
		this.adminCode = adminCode;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDocInfo() {
		return docInfo;
	}

	public void setDocInfo(String docInfo) {
		this.docInfo = docInfo;
	}

	public JsfDocConfigDto getDocConfig() {
		return docConfig;
	}

	public void setDocConfig(JsfDocConfigDto docConfig) {
		this.docConfig = docConfig;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getVisibility() {
		return visibility;
	}

	public void setVisibility(Integer visibility) {
		this.visibility = visibility;
	}

	public String getTenantId() {
		return tenantId;
	}

	public void setTenantId(String tenantId) {
		this.tenantId = tenantId;
	}
}
