package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.soap.common.xml.schema.JsonType;

public class AnyParamConverter implements JsfParamConverter {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        return  value;
    }
}
