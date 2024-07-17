package com.jd.workflow.console.service.ducc.entity;

/**
 * @author by lishihao4
 * @date 2022/4/25
 * DESC
 */
public class ConfigItemParam {

    private String key;
    private String value;
    private String description;

    public void setItem(String key, String value, String description){
        this.key = key;
        this.value = value;
        this.description = description;
    }
    public void updateItem(String value, String description){
        this.value = value;
        this.description = description;
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
