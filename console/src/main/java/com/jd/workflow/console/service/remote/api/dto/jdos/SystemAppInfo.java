package com.jd.workflow.console.service.remote.api.dto.jdos;

import java.io.Serializable;
import java.util.List;

/**
 * @description: JDos 系统应用信息
 * @author: jialixian
 * @create: 2021-11-16 16:49
 **/
public class SystemAppInfo implements Serializable {
    private static final long serialVersionUID = 4493374852022545523L;
    private String id;
    private String systemName;
    private String nickname;
    private String desc;
    private String creator;
    private String systemLevel;
    private String systemOwner;
    private String systemPm;
    private String systemQa;
    private String systemDepartment;
    private List<JDosAppInfo> apps;
    private String systemLevelDesc;

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

    public String getSystemLevel() {
        return systemLevel;
    }

    public void setSystemLevel(String systemLevel) {
        this.systemLevel = systemLevel;
    }

    public String getSystemOwner() {
        return systemOwner;
    }

    public void setSystemOwner(String systemOwner) {
        this.systemOwner = systemOwner;
    }

    public String getSystemPm() {
        return systemPm;
    }

    public void setSystemPm(String systemPm) {
        this.systemPm = systemPm;
    }

    public String getSystemQa() {
        return systemQa;
    }

    public void setSystemQa(String systemQa) {
        this.systemQa = systemQa;
    }

    public String getSystemDepartment() {
        return systemDepartment;
    }

    public void setSystemDepartment(String systemDepartment) {
        this.systemDepartment = systemDepartment;
    }

    public List<JDosAppInfo> getApps() {
        return apps;
    }

    public void setApps(List<JDosAppInfo> apps) {
        this.apps = apps;
    }

    public String getSystemLevelDesc() {
        return systemLevelDesc;
    }

    public void setSystemLevelDesc(String systemLevelDesc) {
        this.systemLevelDesc = systemLevelDesc;
    }
}
