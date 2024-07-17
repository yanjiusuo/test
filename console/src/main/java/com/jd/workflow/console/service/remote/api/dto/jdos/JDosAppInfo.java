package com.jd.workflow.console.service.remote.api.dto.jdos;

import java.io.Serializable;
import java.util.List;

/**
 * @description: JDos 系统应用信息
 * @author: jialixian
 * @create: 2021-11-16 16:49
 **/
public class JDosAppInfo implements Serializable {
    private static final long serialVersionUID = 4493374852022545523L;
    private String id;
    private String systemName;
    private String appName;
    private String nickname;
    private String desc;
    private String creator;
    private String appType;
    private String appOwner;
    private String appOwnerName;
    private String appTester;
    private String appDepartment;
    private List<String> developers;
    private Long appId;
    private String appLanguage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getAppOwner() {
        return appOwner;
    }

    public void setAppOwner(String appOwner) {
        this.appOwner = appOwner;
    }

    public String getAppOwnerName() {
        return appOwnerName;
    }

    public void setAppOwnerName(String appOwnerName) {
        this.appOwnerName = appOwnerName;
    }

    public String getAppTester() {
        return appTester;
    }

    public void setAppTester(String appTester) {
        this.appTester = appTester;
    }

    public String getAppDepartment() {
        return appDepartment;
    }

    public void setAppDepartment(String appDepartment) {
        this.appDepartment = appDepartment;
    }

    public List<String> getDevelopers() {
        return developers;
    }

    public void setDevelopers(List<String> developers) {
        this.developers = developers;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public String getAppLanguage() {
        return appLanguage;
    }

    public void setAppLanguage(String appLanguage) {
        this.appLanguage = appLanguage;
    }
}
