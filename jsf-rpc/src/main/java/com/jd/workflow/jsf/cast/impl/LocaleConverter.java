package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.util.Locale;
import java.util.Map;

public class LocaleConverter implements JsfParamConverter {
    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(!(value instanceof String)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","string")
                    .param("actual",value.getClass().getName());
        }
        String name = (String) value;
        String[] items = name.split("_");
        if (items.length == 1) {
            return new Locale(items[0]);
        } else {
            return items.length == 2 ? new Locale(items[0], items[1]) : new Locale(items[0], items[1], items[2]);
        }

    }

    @Override
    public Object getDemoValue(Class type) {
        return "zh_CN";
    }
}
