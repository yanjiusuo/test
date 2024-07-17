package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.cast.JsfParamConverterRegistry;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.util.ArrayList;
import java.util.List;

public class CollectionParamConverter implements JsfParamConverter {



    @Override
    public Object write(JsonType currentJsonType, Object value) {
        List result = new ArrayList<>();
        if(!(value instanceof List)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","list")
                    .param("actual",value.getClass().getName());
        }
        List list = (List) value;
        ArrayJsonType arrayJsonType = (ArrayJsonType) currentJsonType;
        if(arrayJsonType.getChildren().size() > 1){
            for (int i = 0; i < list.size(); i++) {
                JsonType childType = arrayJsonType.getChildren().get(i);
                JsfParamConverter childConverter = JsfParamConverterRegistry.getConverter(childType);
                result.add(childConverter.write(childType,list.get(i)));
            }

        }else if(arrayJsonType.getChildren().size() == 1){

        }
        for (int i = 0; i < list.size(); i++) {
            if(arrayJsonType.getChildren().size() > 1){
                JsonType childType = arrayJsonType.getChildren().get(i);
                JsfParamConverter childConverter = JsfParamConverterRegistry.getConverter(childType);
                result.add(childConverter.write(childType,list.get(i)));
            }else if(arrayJsonType.getChildren().size() == 1){
                JsonType childType = arrayJsonType.getChildren().get(0);
                JsfParamConverter childConverter = JsfParamConverterRegistry.getConverter(childType);
                if(childConverter == null){
                    result.add(list.get(i));
                }else{
                    result.add(childConverter.write(childType,list.get(i)));
                }

            }else{
                result.add(list.get(i));
            }

        }

        return result;
    }
}
