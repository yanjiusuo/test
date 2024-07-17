package com.jd.workflow.console.service.remote.api.dto.jone;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 应用信息
 *
 * @author: liuhuiqing
 * @date: 2020/8/16
 */
@ApiModel(description = "jone应用信息")
public class AppInfoRpc implements Serializable {
    private static final long serialVersionUID = -3174426049155460912L;
    @ApiModelProperty(name = "id", value = "appId")
    private Long id;
    @ApiModelProperty(name = "appName", value = "应用英文名")
    private String appName;
    @ApiModelProperty(name = "appNameCn", value = "应用中文名")
    private String appNameCn;
    @ApiModelProperty(name = "domain", value = "域名")
    private String domain;
    @ApiModelProperty(name = "appLeaderNm", value = "应用负责人姓名")
    private String appLeaderNm;
    @ApiModelProperty(name = "appLeader", value = "应用负责人erp")
    private String appLeader;
    @ApiModelProperty(name = "appMembersNm", value = "应用参与人姓名列表")
    private List<String> appMembersNm;
    @ApiModelProperty(name = "appMembers", value = "应用参与人erp列表")
    private List<String> appMembers;
    @ApiModelProperty(name = "sysId", value = "应用所属系统id(冗余字段)")
    private Long sysId;
    @ApiModelProperty(name = "sysName", value = "应用所属系统名称(冗余字段)")
    private String sysName;
    @ApiModelProperty(name = "appDepPath", value = "应用所属的部门路径")
    private String appDepPath;
    @ApiModelProperty(name = "deployAppId", value = "部署应用id")
    private Long deployAppId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppNameCn() {
        return appNameCn;
    }

    public void setAppNameCn(String appNameCn) {
        this.appNameCn = appNameCn;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAppLeaderNm() {
        return appLeaderNm;
    }

    public void setAppLeaderNm(String appLeaderNm) {
        this.appLeaderNm = appLeaderNm;
    }

    public String getAppLeader() {
        return appLeader;
    }

    public void setAppLeader(String appLeader) {
        this.appLeader = appLeader;
    }

    public List<String> getAppMembersNm() {
        return appMembersNm;
    }

    public void setAppMembersNm(List<String> appMembersNm) {
        this.appMembersNm = appMembersNm;
    }

    public List<String> getAppMembers() {
        return appMembers;
    }

    public void setAppMembers(List<String> appMembers) {
        this.appMembers = appMembers;
    }

    public Long getSysId() {
        return sysId;
    }

    public void setSysId(Long sysId) {
        this.sysId = sysId;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getAppDepPath() {
        return appDepPath;
    }

    public void setAppDepPath(String appDepPath) {
        this.appDepPath = appDepPath;
    }

    public Long getDeployAppId() {
        return deployAppId;
    }

    public void setDeployAppId(Long deployAppId) {
        this.deployAppId = deployAppId;
    }
}
