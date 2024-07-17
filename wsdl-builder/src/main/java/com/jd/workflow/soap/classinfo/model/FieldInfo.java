package com.jd.workflow.soap.classinfo.model;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

@Data
public class FieldInfo {
    String name;
    String fullTypeName;

    public FieldInfo(String name, String fullTypeName) {
        this.name = name;
        this.fullTypeName = fullTypeName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullTypeName() {
        return fullTypeName;
    }

    public void setFullTypeName(String fullTypeName) {
        this.fullTypeName = fullTypeName;
    }

    public String capitalizeName(){
        return StringUtils.capitalize(name);
    }

}
