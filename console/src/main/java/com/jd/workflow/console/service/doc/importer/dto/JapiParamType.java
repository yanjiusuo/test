package com.jd.workflow.console.service.doc.importer.dto;

import com.jd.jsf.gd.util.StringUtils;
import com.jd.workflow.soap.common.util.StringHelper;

public enum JapiParamType {
    String(0),
    Number(1),
    Object(2),
    Array(3),
    Boolean(4),
    integer(5)
    ;
    private Integer code;

    JapiParamType(Integer code) {
        this.code = code;
    }
    public String toParamType(){
        if(Number.equals(this)) return "double";
        return name().toLowerCase();
    }
    public static JapiParamType fromType(String type,String paramName){
        if(StringUtils.isBlank(type)) return null;
        if(StringHelper.isNotBlank(paramName) && paramName.indexOf("[:INTEGER]" ) != -1){
            return integer;
        }
        int value = Integer.valueOf(type);
        for (JapiParamType japiParamType : values()) {
            if(value == japiParamType.code) return japiParamType;
        }
        return null;
    }
}
