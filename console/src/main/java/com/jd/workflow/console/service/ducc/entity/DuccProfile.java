package com.jd.workflow.console.service.ducc.entity;

/**
 * DuccProfile
 *
 * @author wangxianghui6
 * @date 2022/3/1 5:51 PM
 */
public class DuccProfile extends DuccBase {

    private DuccCode configuration;
    private DuccCode version;
    private String code;
    private String name;
    private String description;

    public DuccCode getConfiguration() {
        return configuration;
    }

    public void setConfiguration(DuccCode configuration) {
        this.configuration = configuration;
    }

    public DuccCode getVersion() {
        return version;
    }

    public void setVersion(DuccCode version) {
        this.version = version;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
