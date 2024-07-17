package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.net.URL;
import java.util.UUID;

public class ClassParamConverter implements JsfParamConverter {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(!(value instanceof String)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","string")
                    .param("actual",value);
        }
        try {
            return Class.forName((String)value);
        } catch (ClassNotFoundException e) {
            throw new TypeConvertException("typeconvert.err_invalid_class_name")
                    .param("prop",currentJsonType.getName())
                    .param("className",value);
        }
    }
}
