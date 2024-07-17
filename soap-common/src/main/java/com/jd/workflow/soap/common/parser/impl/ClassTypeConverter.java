package com.jd.workflow.soap.common.parser.impl;


import com.jd.workflow.soap.common.parser.TypeConverter;
import com.jd.workflow.soap.common.parser.TypeConverterRegistry;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.TypeUtils;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.*;
import java.util.Map;
import java.util.Set;

public class ClassTypeConverter implements TypeConverter {
    @Override
    public boolean match(Type type) {
        return type instanceof Class;
    }

    @Override
    public void convert(Type type, Set<Class<?>> checkSet, BuilderJsonType jsonType, Map<String,Type> boundedTypeVariable) {
        Class parameter = (Class) type;
        if(checkSet.contains(parameter)){// 类出现递归循环了
            jsonType.setType("object");
            return ;
        }
        checkSet.add(parameter);

        jsonType.setClassName(((Class<?>) type).getName());
        if("void".equals(parameter.getCanonicalName())){
            return ;
        }else if((parameter).isArray()){
            jsonType.setType("array");
            Class comClass;
            BuilderJsonType currentType = jsonType;
            for(comClass = parameter.getComponentType(); comClass.isArray(); comClass = comClass.getComponentType()) {
                BuilderJsonType child = new BuilderJsonType();
                child.setType("array");
                child.setName("$$0");
                currentType.addChild(child);
                currentType = child;
            }
            BuilderJsonType componentJsonType = new BuilderJsonType();
            componentJsonType.setName("$$0");
            currentType.addChild(componentJsonType);
            TypeConverterRegistry.processJsonType(comClass,checkSet,componentJsonType,null);

        }else if(parameter.isPrimitive() || TypeUtils.getPrimitiveType(parameter.getCanonicalName()) !=  null){
            jsonType.setType(TypeUtils.getPrimitiveType(parameter.getCanonicalName()));
            if(parameter.isPrimitive()){
                jsonType.setRequired(true);
            }
            if(parameter.isPrimitive()){
                jsonType.setClassName(TypeUtils.getWrapClassName(parameter.getName()));
            }

        }else if(TypeUtils.getSimpleType(parameter)!=null){
            jsonType.setType(TypeUtils.getSimpleType(parameter));
        }else if(parameter.isEnum()){
            jsonType.setType(SimpleParamType.STRING);
            jsonType.setClassName(String.class.getName());
        }else if(TypeUtils.isCollection(parameter)){
            jsonType.setType("array");
            if(!CollectionUtils.isEmpty(jsonType.getGenericTypes())){
                BuilderJsonType childType = (BuilderJsonType) jsonType.getGenericTypes().get(0);
                childType.setName("$$0");
                jsonType.addChild(childType);
            }else{
                BuilderJsonType childType = BuilderJsonType.from("object");
                childType.setName("$$0");
                jsonType.addChild(childType);
            }

        }
        else if(TypeUtils.isMap(parameter)){
            jsonType.setType("object");
            //jsonType.getExtAttrs().put("isMap",true);
            jsonType.getExtAttrs().put("allowEdit",true);
        }/*else if(parameter.getCanonicalName().startsWith("java.")){

        }*/else{
            jsonType.setType("object");
            Field[] fieldList = BeanTool.getFields(parameter);
            for(int i = 0; i < fieldList.length; ++i) {
                Field field = fieldList[i];


                Class<?> fieldClass = field.getType();
                Type fType = field.getGenericType(); // 字段的泛型
                BuilderJsonType child = new BuilderJsonType();
                child.setName(field.getName());
                child.setClassName(fieldClass.getName());
                 TypeConverterRegistry.processJsonType(fType, checkSet, child,boundedTypeVariable);

                jsonType.addChild(child);
            }
        }
       /* if(parentType != null){
            parentType.addChild(jsonType);
        }*/
        checkSet.remove(parameter);


    }


}
