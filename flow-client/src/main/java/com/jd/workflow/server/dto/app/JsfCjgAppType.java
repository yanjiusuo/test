package com.jd.workflow.server.dto.app;

import java.io.Serializable;
import java.util.Date;
public class JsfCjgAppType implements Serializable {
    private Integer id;
    private String appCode;
    private Integer componentId;
    private Integer appType;
    private String pLevel;
    private Date pLevelNextDate;
    private Date pLevelNextTwoDate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public Integer getComponentId() {
        return componentId;
    }

    public void setComponentId(Integer componentId) {
        this.componentId = componentId;
    }

    public Integer getAppType() {
        return appType;
    }

    public void setAppType(Integer appType) {
        this.appType = appType;
    }

    public String getpLevel() {
        return pLevel;
    }

    public void setpLevel(String pLevel) {
        this.pLevel = pLevel;
    }

    public Date getpLevelNextDate() {
        return pLevelNextDate;
    }

    public void setpLevelNextDate(Date pLevelNextDate) {
        this.pLevelNextDate = pLevelNextDate;
    }

    public Date getpLevelNextTwoDate() {
        return pLevelNextTwoDate;
    }

    public void setpLevelNextTwoDate(Date pLevelNextTwoDate) {
        this.pLevelNextTwoDate = pLevelNextTwoDate;
    }
}
