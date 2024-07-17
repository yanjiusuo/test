package com.jd.workflow.jsf.cast.impl;


import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.lang.IVariant;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.lang.type.ObjectTypes;
import com.jd.workflow.soap.common.util.TypeUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PrimitiveParamConverter implements JsfParamConverter {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        String className = currentJsonType.getClassName();
        try {
            if(StringUtils.isEmpty(className)){
                className = SimpleParamType.from(currentJsonType.getType() ).getType().getName();
            }
            return ObjectTypes.convert(value, TypeUtils.getClass(className));
        } catch (ClassNotFoundException e) {
            throw new TypeConvertException("typecovert.err_invalid_class").param("class",className);
        }
    }

    @Override
    public Object getDemoValue(Class type) {
        if( type != null && "java.lang.String".equals(type.getName())){
            return new StringParamConverter().getDemoValue(type);
        }
        Map<Class,Object> demoValues = new HashMap<>();
        demoValues.put(Boolean.class,true);
        demoValues.put(boolean.class,true);
        demoValues.put(Byte.class,(byte)12);
        demoValues.put(byte.class,(byte)12);
        demoValues.put(Short.class,(short)1);
        demoValues.put(short.class,(short)1);
        demoValues.put(Integer.class,0);
        demoValues.put(int.class,0);
        demoValues.put(Long.class,3L);
        demoValues.put(long.class,3L);
        demoValues.put(Float.class,1.1);
        demoValues.put(float.class,1.1);
        demoValues.put(Double.class,1.2);
        demoValues.put(double.class,1.2);
        demoValues.put(Character.class,'c');
        demoValues.put(char.class,'c');
        demoValues.put(String.class,"");


        return demoValues.get(type);
    }
}
