package com.jd.workflow.server.dto;

public class JsfDocInfo {
    /**
     * jsf接口对应的id
     */
    private Long id;
    /**
     * jsf接口的访问地址
     */
    private String url;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
