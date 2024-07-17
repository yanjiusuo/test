package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class TimeZoneConverter implements JsfParamConverter {



    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(!(value instanceof String)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","string")
                    .param("actual",value.getClass().getName());
        }
        return TimeZone.getTimeZone((String)value);
    }
}
