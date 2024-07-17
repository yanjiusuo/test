package com.jd.workflow.server.dto.interfaceManage;


import java.util.Map;
import java.util.Set;

/**
 * @date: 2022/5/16 18:38
 * @author wubaizhao1
 */
public class JsfMethodManage {

	/**
	 * methodId
	 */
	private Long id;
	/**
	 * 所属应用编码
	 */
	private String appCode;

	/**
	 * 所属应用id
	 */
	private Long appId;
	/**
	 * 所属应用名称
	 */
	private String appName;


	/**
	 * 所属的接口id
	 */
	private Long interfaceId;

	/**
	 * 分组id
	 */
	private Long groupId;
	/**
	 * 标签集合
	 */
	private Set<String> tags;
	/**
	 * 接口类型 1-http、2-webservice、3-jsf 10-编排 22 扩展点
	 * InterfaceTypeEnum
	 */
	private Integer type;
	/**
	 * 方法编码
	 */
	private String methodCode;

	/**
	 * 方法名称
	 */
	private String name;

	/**
	 * 方法描述
	 * ws 自动生成的时候，为服务名
	 */
	private String desc;

	/**
	 * 文档描述
	 */
	private String docInfo;

	/**
	 * 是否有操作权限（不存入表中）  1-有  2-无
	 */
	private Integer hasAuth;
	/**
	 * 是否demo 1-是 0-否
	 */
	private Integer isPublic;

	/**
	 * 请求方式 get post put 等
	 */
	private String httpMethod;


	/**
	 * 输出时为空，入参用
	 * 方法内容 json信息 [大字段]
	 */
	private JsfHttpMethodModel content;

	/**
	 * 修改的差量信息
	 */
	private Map<String,Object> _delta;
	/**
	 * 方法路径
	 */
	private String path;

	/**
	 * 父方法id
	 */
	private Long parentId;

	/**
	 * 生成的wsdl地址
	 */
//	private String wsdlUrl;

	/**
	 * 是否发布 0-未发布 1-已发布
	 */
	private Integer published;

	/**
	 * 调用地址 发布后才能调用
	 */
//	private String endpointUrl;

	/**
	 * 调用环境 http转webservice才有调用环境
	 */
	private String callEnv;

	/**
	 * 参数个数 webservice方法存放
	 */
	private Integer paramCount;

	/**
	 * 额外配置
	 */
	private String extConfig;
	/**
	 * 文档配置，包括出入参示例
	 */
	private JsfMethodDocConfig docConfig;

	/**
	 * 上报状态
	 */
	Integer reportStatus;
	/**
	 * 是否自动上报：1-是 0-否
	 */
	Integer autoReport;
	/**
	 * 接口状态
	 * ENABLED(1,"已上线")
	 * ABANDON(3,"已废弃"),
	 * DEVELOPING(5,"开发中"),
	 * TEST(6,"测试中"),
	 */
	private Integer status;
	// http mock路径

	private String httpMockPath;
	// easy的mock别名

	private String jsfMockAlias;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAppCode() {
		return appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	public Long getAppId() {
		return appId;
	}

	public void setAppId(Long appId) {
		this.appId = appId;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Long getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(Long interfaceId) {
		this.interfaceId = interfaceId;
	}

	public Long getGroupId() {
		return groupId;
	}

	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	public Set<String> getTags() {
		return tags;
	}

	public void setTags(Set<String> tags) {
		this.tags = tags;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getMethodCode() {
		return methodCode;
	}

	public void setMethodCode(String methodCode) {
		this.methodCode = methodCode;
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

	public String getDocInfo() {
		return docInfo;
	}

	public void setDocInfo(String docInfo) {
		this.docInfo = docInfo;
	}

	public Integer getHasAuth() {
		return hasAuth;
	}

	public void setHasAuth(Integer hasAuth) {
		this.hasAuth = hasAuth;
	}

	public Integer getIsPublic() {
		return isPublic;
	}

	public void setIsPublic(Integer isPublic) {
		this.isPublic = isPublic;
	}

	public String getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}

	public JsfHttpMethodModel getContent() {
		return content;
	}

	public void setContent(JsfHttpMethodModel content) {
		this.content = content;
	}

	public Map<String, Object> get_delta() {
		return _delta;
	}

	public void set_delta(Map<String, Object> _delta) {
		this._delta = _delta;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Long getParentId() {
		return parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public Integer getPublished() {
		return published;
	}

	public void setPublished(Integer published) {
		this.published = published;
	}

	public String getCallEnv() {
		return callEnv;
	}

	public void setCallEnv(String callEnv) {
		this.callEnv = callEnv;
	}

	public Integer getParamCount() {
		return paramCount;
	}

	public void setParamCount(Integer paramCount) {
		this.paramCount = paramCount;
	}

	public String getExtConfig() {
		return extConfig;
	}

	public void setExtConfig(String extConfig) {
		this.extConfig = extConfig;
	}

	public JsfMethodDocConfig getDocConfig() {
		return docConfig;
	}

	public void setDocConfig(JsfMethodDocConfig docConfig) {
		this.docConfig = docConfig;
	}

	public Integer getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(Integer reportStatus) {
		this.reportStatus = reportStatus;
	}

	public Integer getAutoReport() {
		return autoReport;
	}

	public void setAutoReport(Integer autoReport) {
		this.autoReport = autoReport;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getHttpMockPath() {
		return httpMockPath;
	}

	public void setHttpMockPath(String httpMockPath) {
		this.httpMockPath = httpMockPath;
	}

	public String getJsfMockAlias() {
		return jsfMockAlias;
	}

	public void setJsfMockAlias(String jsfMockAlias) {
		this.jsfMockAlias = jsfMockAlias;
	}
}
