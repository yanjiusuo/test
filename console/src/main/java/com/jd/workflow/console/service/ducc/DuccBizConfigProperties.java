package com.jd.workflow.console.service.ducc;

import com.jd.workflow.console.service.ducc.enums.ProfileEnum;

/**
 * DuccBizConfigProperties
 *
 * @author wangxianghui6
 * @date 2022/3/2 10:16 AM
 */
public class DuccBizConfigProperties {

    private String application;

    private String token;

    private String host;

    private String namespaceId;

    private ProfileEnum profile;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public ProfileEnum getProfile() {
        return profile;
    }

    public void setProfile(ProfileEnum profile) {
        this.profile = profile;
    }
}
