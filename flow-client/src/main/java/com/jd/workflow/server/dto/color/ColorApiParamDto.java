package com.jd.workflow.server.dto.color;


import java.io.Serializable;
import java.util.Date;

public class ColorApiParamDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;
    /**
     * 接口名称
     */
    private String name;
    /**
     * 旧版api文档id
     */
    private String apiId;
    /**
     * 方法id
     */
    private String methodId;
    /**
     * colorAPi
     */
    private String api;
    /**
     * 环境 pre、pro
     */
    private String zone;
    /**
     * 接口描述
     */
    private String description;

    /**
     * 参数类型   1-requestHeader 2-requestParam 3-responseHeader'
     */
    private Integer type;

    /**
     * 1-网关规范 2、用户自定义 3-http协议
     */
    private Integer mark;
    /**
     * 1-传递 0-否
     */

    private Integer isTransparent;
    /**
     * 1-必填 0-否
     */
    private Integer isAppNecessary;
    /**
     * 1-网关生成 2-客户端传递
     */
    private Integer source;
    /**
     * 1-可编辑 0-不可编辑
     */
    private Integer isEdit;
    /**
     * 字段类型 string 、integer
     */
    private String dataType;

    private Date created;

    private String creator;

    private Integer defaultShow;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getApiId() {
        return apiId;
    }

    public void setApiId(String apiId) {
        this.apiId = apiId;
    }

    public String getMethodId() {
        return methodId;
    }

    public void setMethodId(String methodId) {
        this.methodId = methodId;
    }

    public String getApi() {
        return api;
    }

    public void setApi(String api) {
        this.api = api;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getMark() {
        return mark;
    }

    public void setMark(Integer mark) {
        this.mark = mark;
    }

    public Integer getIsTransparent() {
        return isTransparent;
    }

    public void setIsTransparent(Integer isTransparent) {
        this.isTransparent = isTransparent;
    }

    public Integer getIsAppNecessary() {
        return isAppNecessary;
    }

    public void setIsAppNecessary(Integer isAppNecessary) {
        this.isAppNecessary = isAppNecessary;
    }

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
    }

    public Integer getIsEdit() {
        return isEdit;
    }

    public void setIsEdit(Integer isEdit) {
        this.isEdit = isEdit;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public Integer getDefaultShow() {
        return defaultShow;
    }

    public void setDefaultShow(Integer defaultShow) {
        this.defaultShow = defaultShow;
    }
}

