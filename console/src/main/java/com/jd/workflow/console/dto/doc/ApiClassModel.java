package com.jd.workflow.console.dto.doc;


import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;

import java.util.List;
import java.util.Objects;

public class ApiClassModel {
    String className;
    ObjectJsonType model;
    String desc;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApiClassModel apiModel = (ApiClassModel) o;
        return Objects.equals(className, apiModel.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public ObjectJsonType getModel() {
        return model;
    }

    public void setModel(ObjectJsonType model) {
        this.model = model;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
