package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.lang.type.ObjectTypes;
import com.jd.workflow.soap.common.xml.schema.JsonType;

public class StackTraceConverter implements JsfParamConverter {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        throw new TypeConvertException("typeconvert.err_stacktrace_param_not_support").param("class",currentJsonType.getClassName());

    }
}
