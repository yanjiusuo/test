package com.jd.workflow.method;

import java.util.List;

public class MethodInfo {

    private Long id;

    /**
     * 接口类型 1-http、2-webservice、3-jsf 10-编排
     * link{@com.jd.workflow.console.base.enums.InterfaceTypeEnum}
     */
    private Integer type;

    /**
     * 方法名称
     */
    private String name;
    /**
     * 方法编码- 英文名称
     */
    private String methodCode;

    /**
     * 方法描述
     */
    private String desc;

    /**
     * 是否有操作权限（不存入表中）  1-有  2-无
     */
    private Integer hasAuth;

    /**
     * 请求方式 GET POST PUT等
     */
    private String httpMethod;

    /**
     * 所属的接口id
     */
    private Long interfaceId;


    private Object contentObject;

    /**
     * 方法路径
     */
    private String path;
    String inputExample;
    String outputExample;

    /**
     * 接口状态
     */
    private Integer status;

    /**
     * 额外配置
     */
    private String extConfig;
    /**
     * 关联id,目前用来存导入记录的id
     */
    private Long relatedId;


    /**
     * 接口上报同步状态:1-不同步(默认) 0-同步
     */
    private Integer reportSyncStatus;

    /**
     * 文档描述
     */
    String docInfo;
    /**
     * 健康度：0-100
     */
    double score;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public void setMethodCode(String methodCode) {
        this.methodCode = methodCode;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getHasAuth() {
        return hasAuth;
    }

    public void setHasAuth(Integer hasAuth) {
        this.hasAuth = hasAuth;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public Long getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(Long interfaceId) {
        this.interfaceId = interfaceId;
    }

    public Object getContentObject() {
        return contentObject;
    }

    public void setContentObject(Object contentObject) {
        this.contentObject = contentObject;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getInputExample() {
        return inputExample;
    }

    public void setInputExample(String inputExample) {
        this.inputExample = inputExample;
    }

    public String getOutputExample() {
        return outputExample;
    }

    public void setOutputExample(String outputExample) {
        this.outputExample = outputExample;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getExtConfig() {
        return extConfig;
    }

    public void setExtConfig(String extConfig) {
        this.extConfig = extConfig;
    }

    public Long getRelatedId() {
        return relatedId;
    }

    public void setRelatedId(Long relatedId) {
        this.relatedId = relatedId;
    }

    public Integer getReportSyncStatus() {
        return reportSyncStatus;
    }

    public void setReportSyncStatus(Integer reportSyncStatus) {
        this.reportSyncStatus = reportSyncStatus;
    }

    public String getDocInfo() {
        return docInfo;
    }

    public void setDocInfo(String docInfo) {
        this.docInfo = docInfo;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
