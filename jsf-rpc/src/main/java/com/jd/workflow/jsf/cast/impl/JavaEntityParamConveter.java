package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;

import java.util.HashMap;
import java.util.Map;

public class JavaEntityParamConveter implements JsfParamConverter {



    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(!(value instanceof Map)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","map")
                    .param("actual",value.getClass().getName());
        }
        Map map = (Map) value;
        ObjectJsonType jsonType = (ObjectJsonType) currentJsonType;
        Map<String,Object> ressult = new HashMap<>();
        ressult.put("class",currentJsonType.getClassName());
        for (JsonType child : jsonType.getChildren()) {
            ressult.put(child.getName(), JsfParamConverterRegistry.convertValue(child,map.get(child.getName())));

        }
        return ressult;
    }
}
