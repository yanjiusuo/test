package com.jd.workflow.codegen.model;

import com.jd.workflow.console.dto.HttpMethodModel;

import java.util.ArrayList;
import java.util.List;

public class GroupInterfaceData {
    String groupName;
    String pkgName;
    String groupDesc;
    List<HttpMethodModel> methods = new ArrayList<>();

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupDesc() {
        return groupDesc;
    }

    public void setGroupDesc(String groupDesc) {
        this.groupDesc = groupDesc;
    }

    public List<HttpMethodModel> getMethods() {
        return methods;
    }

    public void setMethods(List<HttpMethodModel> methods) {
        this.methods = methods;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }
}
