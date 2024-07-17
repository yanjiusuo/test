package com.jd.workflow.soap.common.parser.impl;

import com.jd.workflow.soap.common.parser.TypeConverter;
import com.jd.workflow.soap.common.parser.TypeConverterRegistry;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import java.util.Set;
@Slf4j
public class TypeVariableConverter implements TypeConverter {
    @Override
    public boolean match(Type type) {
        return type instanceof TypeVariable;
    }



    @Override
    public void convert(Type type, Set<Class<?>> checkSet, BuilderJsonType currentType, Map<String, Type> boundTypeVariable) {
        log.info("jsontype.process_type_variable:{}",type);
        final Type actualType = boundTypeVariable.get(type.getTypeName());
        if(actualType != null){
            TypeConverterRegistry.processJsonType(actualType, checkSet, currentType, null);
        }else{
            currentType.setType("object");
        }
    }

}
