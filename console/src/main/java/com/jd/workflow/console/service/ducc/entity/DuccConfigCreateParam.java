package com.jd.workflow.console.service.ducc.entity;

/**
 * CreateDuccConfigParam
 *
 * @author wangxianghui6
 * @date 2022/3/1 3:35 PM
 */
public class DuccConfigCreateParam {

    private String code;

    private String name;

    private String description;

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
