package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

public class CharArrayParamConverter implements JsfParamConverter {
    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(value instanceof List) return value;
        if(!(value instanceof String)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                   .param("expected","string")
                    .param("actual",value.getClass().getName());
        }
        String str = (String) value;
        return str.toCharArray();
    }


}
