package com.jd.workflow.console.service.doc.importer.dto;

import com.jd.jsf.gd.util.StringUtils;

public enum JapiHttpMethod {
    POST(0),
    GET(1),
    PUT(2),
    DELETE(3),
    HEAD(4),
    OPTIONS(5),
    PATCH(6)
    ;
    private Integer code;

    JapiHttpMethod(Integer code) {
        this.code = code;
    }

    public static JapiHttpMethod fromType(Integer type){
        if(type == null) return null;
        for (JapiHttpMethod japiParamType : values()) {
            if(type.equals(japiParamType.code)) return japiParamType;
        }
        return null;
    }
}
