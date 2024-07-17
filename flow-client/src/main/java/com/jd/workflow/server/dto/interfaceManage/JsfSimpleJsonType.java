package com.jd.workflow.server.dto.interfaceManage;


/**
 * 简单类型: string、number、integer、boolean
 */

public class JsfSimpleJsonType extends JsfJsonType {

    public JsfSimpleJsonType(String type) {
        super(type);
    }

    public boolean isSimpleType() {
        return true;
    }


}
