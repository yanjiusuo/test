package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.util.MathHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.net.URL;

public class StringParamConverter implements JsfParamConverter<URL> {
    private static String SYMBOL = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(!(value instanceof String)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","string")
                    .param("actual",value);
        }
        return value;
    }
     public Object getDemoValue(Class type){
        int length = MathHelper.random().nextInt(20);
        StringBuilder sb = new StringBuilder();
         for (int i = 0; i < length; i++) {
             int index = MathHelper.random().nextInt(SYMBOL.length()-1);
             sb.append(SYMBOL.charAt(index));
         }
        return sb.toString();
    }
}
