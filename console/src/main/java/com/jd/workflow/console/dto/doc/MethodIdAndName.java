package com.jd.workflow.console.dto.doc;

public class MethodIdAndName {
    /**
     * 方法id
     */
    Long id;
    /**
     * 方法名称
     */
    String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
