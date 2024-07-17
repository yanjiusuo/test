package com.jd.workflow.console.dto.auth;

import lombok.Data;

import javax.validation.constraints.NotNull;


public class AuthDto {
    boolean enableAuth;
    String relatedAppId;//关联应用id
    String relatedAppName;//关联应用名称
    String developerMaster;//开发负责人
    String productMaster;//产品负责人
    boolean isPublic;// 是否公开
    @NotNull(message = "接口id不可为空")
    String id;//id

    public boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean aPublic) {
        isPublic = aPublic;
    }

    public boolean isEnableAuth() {
        return enableAuth;
    }

    public void setEnableAuth(boolean enableAuth) {
        this.enableAuth = enableAuth;
    }

    public String getRelatedAppId() {
        return relatedAppId;
    }

    public void setRelatedAppId(String relatedAppId) {
        this.relatedAppId = relatedAppId;
    }

    public String getRelatedAppName() {
        return relatedAppName;
    }

    public void setRelatedAppName(String relatedAppName) {
        this.relatedAppName = relatedAppName;
    }

    public String getDeveloperMaster() {
        return developerMaster;
    }

    public void setDeveloperMaster(String developerMaster) {
        this.developerMaster = developerMaster;
    }

    public String getProductMaster() {
        return productMaster;
    }

    public void setProductMaster(String productMaster) {
        this.productMaster = productMaster;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
