package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.net.URL;
import java.util.UUID;

public class UuidParamConverter implements JsfParamConverter<URL> {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(!(value instanceof String)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","string")
                    .param("actual",value);
        }
        return UUID.fromString((String)value);
    }

    @Override
    public Object getDemoValue(Class type) {
        return UUID.randomUUID().toString();
    }
}
