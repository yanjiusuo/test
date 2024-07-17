package com.jd.workflow.server.dto;

/**
 * jsf或者http接口
 */
public class JsfOrHttpInfo {
    /**
     * id
     */
    private Long id;
    /**
     * 接口名称
     */
    String name;
    /**
     * http请求方法
     */
    String httpMethod;
    /**
     * 接口路径
     */
    String path;
    /**
     * 接口在需求里的url地址
     */
    String url;

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

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
