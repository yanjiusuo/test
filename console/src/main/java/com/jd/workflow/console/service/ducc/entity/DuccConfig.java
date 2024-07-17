package com.jd.workflow.console.service.ducc.entity;

/**
 * DuccConfig
 *
 * @author wangxianghui6
 * @date 2022/3/1 3:41 PM
 */
public class DuccConfig extends DuccBase {

    private String code;
    private String name;
    private DuccCode namespace;
    private DuccCode owner;

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

    public DuccCode getNamespace() {
        return namespace;
    }

    public void setNamespace(DuccCode namespace) {
        this.namespace = namespace;
    }

    public DuccCode getOwner() {
        return owner;
    }

    public void setOwner(DuccCode owner) {
        this.owner = owner;
    }
}
