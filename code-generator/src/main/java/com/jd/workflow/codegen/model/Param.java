package com.jd.workflow.codegen.model;

import com.jd.workflow.codegen.model.type.IClassModel;
import lombok.Data;

@Data
public class Param {
    String name;
    String desc;
    /**
     * path、query、body
     */
    String paramType;
    IClassModel type;
    public String getReference(){
        return type.getReference();
    }
    public String getJsType(){
        return type.getJsType();
    }

    public String getName() {
        return name;
    }

    public IClassModel getType() {
        return type;
    }

    public void setType(IClassModel type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTsInterface(){
        return null;
    }
}
