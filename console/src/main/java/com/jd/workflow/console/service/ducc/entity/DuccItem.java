package com.jd.workflow.console.service.ducc.entity;

/**
 * DuccItem
 *
 * @author wangxianghui6
 * @date 2022/3/1 3:59 PM
 */
public class DuccItem extends DuccBase {
    private DuccCode profile;
    private String key;
    private String value;
    private String description;

    public DuccCode getProfile() {
        return profile;
    }

    public void setProfile(DuccCode profile) {
        this.profile = profile;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
