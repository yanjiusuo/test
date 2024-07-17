package com.jd.workflow.server.dto;


import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * jsf或者http接口信息
 */
public class JsfOrHttpInterfaceInfo {
    /**
     * 主键
     */

    private Long id;

    /**
     * 接口类型,枚举字段 1-http 3-jsf
     */
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
     * jsf接口maven坐标，格式：groupId:artifactId:version
     */
    private String path;

    private String docInfo;


    /**
     * 接口分组编码
     *
     * @date: 2022/6/2 15:56
     * @author wubaizhao1
     */
    private String serviceCode;
    /**
     * 文档类型：md或者html
     */
    String docType;
    /**
     * 关联藏经阁appId
     */
    private String cjgAppId;
    /**
     * 关联藏经阁appName
     */
    private String cjgAppName;

    /**
     * 是否自动上报：1-是 0-否
     */
    Integer autoReport;

    Long appId;

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

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Integer isPublic) {
        this.isPublic = isPublic;
    }

    public Integer getHasAuth() {
        return hasAuth;
    }

    public void setHasAuth(Integer hasAuth) {
        this.hasAuth = hasAuth;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getCjgAppId() {
        return cjgAppId;
    }

    public void setCjgAppId(String cjgAppId) {
        this.cjgAppId = cjgAppId;
    }

    public String getCjgAppName() {
        return cjgAppName;
    }

    public void setCjgAppName(String cjgAppName) {
        this.cjgAppName = cjgAppName;
    }

    public Integer getAutoReport() {
        return autoReport;
    }

    public void setAutoReport(Integer autoReport) {
        this.autoReport = autoReport;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getDocInfo() {
        return docInfo;
    }

    public void setDocInfo(String docInfo) {
        this.docInfo = docInfo;
    }
}
