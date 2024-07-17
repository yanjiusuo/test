package com.jd.workflow.server.dto.app;

import java.io.Serializable;

public class JsfJdosAppSysInfo implements Serializable {
    private String appName;
    private Integer platform;
    private String jdosSite;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Integer getPlatform() {
        return platform;
    }

    public void setPlatform(Integer platform) {
        this.platform = platform;
    }

    public String getJdosSite() {
        return jdosSite;
    }

    public void setJdosSite(String jdosSite) {
        this.jdosSite = jdosSite;
    }
}
