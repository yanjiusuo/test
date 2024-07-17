package com.jd.workflow.soap.common.parser.impl;

import com.jd.workflow.soap.common.parser.TypeConverter;
import com.jd.workflow.soap.common.parser.TypeConverterRegistry;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.Set;

public class WildcardTypeConverter implements TypeConverter {
    @Override
    public boolean match(Type type) {
        return type instanceof WildcardType;
    }

    @Override
    public void convert(Type type, Set<Class<?>> checkSet, BuilderJsonType currentType, Map<String,Type> boundTypeVariable) {
        WildcardType parameter = (WildcardType) type;
        Type[] lowerBounds = parameter.getLowerBounds();
        Type[] upperBounds = parameter.getUpperBounds();
        if(lowerBounds != null && lowerBounds.length == 1){
            TypeConverterRegistry.processJsonType(lowerBounds[0],checkSet,currentType,null);
        }else if(upperBounds != null && upperBounds.length == 1){
            TypeConverterRegistry.processJsonType(upperBounds[0],checkSet,currentType,null);
        }else{
            currentType.setType("Object");
            currentType.setType("java.lang.Object");
        }


    }


}
