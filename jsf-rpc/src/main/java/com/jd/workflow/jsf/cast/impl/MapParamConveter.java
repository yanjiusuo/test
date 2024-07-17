package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;

import java.util.HashMap;
import java.util.Map;

public class MapParamConveter implements JsfParamConverter {



    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(!(value instanceof Map)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","map")
                    .param("actual",value.getClass().getName());
        }
        Map mapValue = (Map) value;
        JsonType keyType=null,valueType = null;
        if(currentJsonType.getGenericTypes()!= null){
            if(currentJsonType.getGenericTypes().size() >= 1){
                keyType = currentJsonType.getGenericTypes().get(0);
            }
            if(currentJsonType.getGenericTypes().size() == 2){
                valueType = currentJsonType.getGenericTypes().get(1);
            }
        }
        Map result = new HashMap();
        for (Object o : mapValue.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object itemKey = entry.getKey();
            Object itemValue = entry.getValue();
            if(keyType != null){
                itemKey = JsfParamConverterRegistry.convertValue(keyType,itemKey);
            }
            if(valueType != null){
                itemValue = JsfParamConverterRegistry.convertValue(valueType,itemValue);
            }
            result.put(itemKey,itemValue);
        }
        return result;
    }
}
