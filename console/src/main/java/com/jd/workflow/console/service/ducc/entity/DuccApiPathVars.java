package com.jd.workflow.console.service.ducc.entity;

/**
 * DuccApiPathVars
 *
 * @author wangxianghui6
 * @date 2022/3/2 10:56 AM
 */
public class DuccApiPathVars {
    private String namespaceId;
    private String configId;
    private String profileId;
    private String itemId;

    public DuccApiPathVars() {
    }

    public String getNamespaceId() {
        return namespaceId;
    }

    public void setNamespaceId(String namespaceId) {
        this.namespaceId = namespaceId;
    }

    public String getConfigId() {
        return configId;
    }

    public void setConfigId(String configId) {
        this.configId = configId;
    }

    public String getProfileId() {
        return profileId;
    }

    public void setProfileId(String profileId) {
        this.profileId = profileId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
