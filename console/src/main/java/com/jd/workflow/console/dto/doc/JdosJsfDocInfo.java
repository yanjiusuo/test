package com.jd.workflow.console.dto.doc;

import java.util.List;

public class JdosJsfDocInfo {
    private Long id;
    List<MethodIdAndName> methods;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<MethodIdAndName> getMethods() {
        return methods;
    }

    public void setMethods(List<MethodIdAndName> methods) {
        this.methods = methods;
    }
}
