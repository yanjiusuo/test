package com.jd.workflow.soap.common.parser.impl;

import com.jd.workflow.soap.common.parser.TypeConverter;
import com.jd.workflow.soap.common.parser.TypeConverterRegistry;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParameterizedTypeConverter implements TypeConverter {
    @Override
    public boolean match(Type type) {
        return type instanceof ParameterizedType;
    }

    @Override
    public void convert(Type type, Set<Class<?>> checkSet, BuilderJsonType jsonType, Map<String,Type> boundTypeVariable) {
        ParameterizedType parameter = (ParameterizedType) type;
        Type actualType = parameter.getRawType();

         TypeVariable[] typeParameters = null;
        if(actualType instanceof Class){
            typeParameters = ((Class) actualType).getTypeParameters();
        }
        //if(checkSet.contains(actualType)) return null;
       /* BuilderJsonType jsonType = new BuilderJsonType();*/
        jsonType.setClassName((((Class)actualType)).getName());

        jsonType.setGenericTypes(new ArrayList<>());
        Map<String,Type> types = new HashMap<>();
        int index = 0;
        for (Type actualTypeArgument : parameter.getActualTypeArguments()) {
            if(actualTypeArgument instanceof TypeVariable){ // 存在嵌套时，获取嵌套类型的实际类型。
                Type boundType = boundTypeVariable.get(actualTypeArgument.getTypeName());
                if(boundType != null){
                    actualTypeArgument = boundType;
                }
            }
            BuilderJsonType child = new BuilderJsonType();
            jsonType.getGenericTypes().add(child);
            if(typeParameters != null){
                types.put(typeParameters[index].getTypeName(),actualTypeArgument);
            }
            TypeConverterRegistry.processJsonType(actualTypeArgument, checkSet, child,boundTypeVariable);
            index++;
        }
         TypeConverterRegistry.processJsonType(actualType, checkSet, jsonType,types);

    }


}
