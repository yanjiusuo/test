package com.jd.workflow.soap.common.parser;

import com.jd.workflow.soap.common.parser.impl.*;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.lang.reflect.Type;
import java.util.*;

public class TypeConverterRegistry {
    static List<TypeConverter> converters = new ArrayList<>();

    static {
        register(new ClassTypeConverter());
        register(new GenericArrayTypeConverter());
        register(new ParameterizedTypeConverter());
        register(new TypeVariableConverter());
        register(new WildcardTypeConverter());
    }

    public static void register(TypeConverter typeConverter){
        converters.add(typeConverter);
    }
    public static void processJsonType(Type type, Set<Class<?>> checkSet, BuilderJsonType parentType, Map<String,Type> boundedTypeVariable){
        if(boundedTypeVariable == null){
            boundedTypeVariable = Collections.emptyMap();
        }
        for (TypeConverter converter : converters) {
            if(converter.match(type)){
                 converter.convert(type,checkSet,parentType,boundedTypeVariable);
                 return;
            }
        }
    }
    public static JsonType processJsonType(Type type){
        BuilderJsonType builderJsonType = new BuilderJsonType();
        builderJsonType.setName("root");
        processJsonType(type,new HashSet<>(),builderJsonType,new HashMap<>());
        return builderJsonType.toJsonType();
    }

}
