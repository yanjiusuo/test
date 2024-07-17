package com.jd.workflow.jsf.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.jsf.gd.util.CodecUtils;
import com.jd.jsf.gd.util.TelnetUtils;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class FieldParserUtils {
    private static Logger logger = LoggerFactory.getLogger(TelnetUtils.class);

    public FieldParserUtils() {
    }
    public static void processType(Type param, Set<Class<?>> checkSet, BuilderJsonType paramType){
        if (param instanceof Class) {
            Class paramClass = (Class)param;

            paramType.setName(paramClass.getCanonicalName());
            if (!paramClass.isArray()) { // 非数组类型，需要解析字段列表
                scanParameter(paramClass, new HashSet(),paramType);
            } else {
                paramType.setType("array");
                BuilderJsonType currentType = paramType;
                Class comClass;
                for(comClass = paramClass.getComponentType(); comClass.isArray(); comClass = comClass.getComponentType()) {
                    BuilderJsonType childType = new BuilderJsonType();
                    childType.setType("array");
                    currentType.addChild(childType);
                    currentType = childType;
                }

                BuilderJsonType arrayItemType = new BuilderJsonType();
                currentType.addChild(arrayItemType);

                scanParameter(comClass, new HashSet(), arrayItemType);
            }
        } else {

            if (!(param instanceof GenericArrayType)) {


                getGenericField(param,  checkSet,paramType);
            } else {


                GenericArrayType compsType = (GenericArrayType)param;
                Type realType = compsType.getGenericComponentType();
                if (realType instanceof GenericArrayType) {
                    while(realType instanceof GenericArrayType) {
                        realType = ((GenericArrayType)realType).getGenericComponentType();
                    }
                }

                if (realType instanceof Class) {
                    Class realClass = (Class)realType;
                    scanParameter(realClass, new HashSet(),paramType);
                } else if (realType instanceof ParameterizedType) {
                    String realStr = realType.toString();
                    getGenericField(realType, new HashSet(),paramType);
                }
            }
        }
    }
    public static void scanParameter(Class<?> parameter, Set<Class<?>> checkSet, BuilderJsonType parentType) {
        if (!checkSet.contains(parameter) && !parameter.isPrimitive() && !parameter.isEnum() && !parameter.getCanonicalName().startsWith("java.")) {
            checkSet.add(parameter);
            Field[] fieldList = CodecUtils.getFields(parameter);

            parentType.setType("object");

            parentType.setName(parameter.getSimpleName());



            for(int i = 0; i < fieldList.length; ++i) {
                BuilderJsonType child = new BuilderJsonType();
                parentType.addChild(child);
                Field field = fieldList[i];
                Class<?> fieldClass = field.getType();
                Type fType = field.getGenericType();


                boolean isContainerType = processContainerType(fieldClass,child);
                if (fType instanceof ParameterizedType) { // 泛型
                    ParameterizedType pfType = (ParameterizedType)fType;
                    String typeStr = pfType.toString();

                    child.setName(field.getName());
                    child.setClassName(typeStr);
                    getGenericField(pfType,  checkSet,child);
                    if(isContainerType){
                        if(isCollection(fieldClass) && !CollectionUtils.isEmpty(child.getGenericTypes())){ // 集合类型需要有child, map类型则不需要了
                            child.addChild((BuilderJsonType) child.getGenericTypes().get(0));
                        }
                    }
                } else {


                    child.setName(field.getName());
                    child.setClassName(fieldClass.getCanonicalName());
                    if (!fieldClass.isPrimitive() && !fieldClass.isEnum() && !fieldClass.getCanonicalName().startsWith("java.") && !fieldClass.equals(parameter)) {
                        if (!fieldClass.isArray()) {
                            scanParameter(fieldClass, checkSet, child);

                        } else {

                            Class comClass;

                            for(comClass = fieldClass.getComponentType(); comClass.isArray(); comClass = comClass.getComponentType()) {

                            }


                            BuilderJsonType arrayItemType = processArrayType(fieldClass,child);
                            scanParameter(comClass, checkSet, arrayItemType);
                        }
                    }else{

                        processJavaType(fieldClass,child,checkSet);
                    }
                }
            }


        }else{
            processJavaType(parameter,parentType,checkSet);
        }
    }
    static boolean processContainerType(Class fieldClass,BuilderJsonType currentType){
        if(isCollection(fieldClass)){
            currentType.setType("array");
        }else if(isMap(fieldClass)){
            currentType.setType("object");
        }else{
            return false;
        }
        return true;

    }
    private static boolean isCollection(Class clazz){
        return Collection.class.isAssignableFrom(clazz);
    }

    private static boolean isMap(Class clazz){
        return  Map.class.isAssignableFrom(clazz);
    }

    static BuilderJsonType processArrayType(Class fieldClass, BuilderJsonType currentType){
        Class comClass;
        currentType.setType("array");
        for(comClass = fieldClass.getComponentType(); comClass.isArray(); comClass = comClass.getComponentType()) {
            BuilderJsonType jsonType = new BuilderJsonType();
            jsonType.setType("array");
            currentType.addChild(jsonType);
            currentType = jsonType;
        }
        BuilderJsonType arrayItemType = new BuilderJsonType();
        currentType.addChild(arrayItemType);
        return arrayItemType;
    }
    static void processJavaType(Class<?> parameter,BuilderJsonType jsonType, Set<Class<?>> checkSet){
        if(parameter.isArray()){
            Class comClass;
            for(comClass = parameter.getComponentType(); comClass.isArray(); comClass = comClass.getComponentType()) {

            }
            BuilderJsonType childType = processArrayType(parameter, jsonType);
            scanParameter(comClass,checkSet,childType);
        }else{
            if(isCollection(parameter)){
                jsonType.setType("array");
                BuilderJsonType child = new BuilderJsonType();
                child.setType("object");
                jsonType.addChild(child);
            }else if(isMap(parameter)){
                jsonType.setType("object");
            }
            jsonType.setType(parameter.getTypeName());
            if(parameter.isPrimitive()){

            }
        }


    }
    /*public static void getGenericField(String typeStr, Set<Class<?>> checkSet,BuilderJsonType child) {
        if (typeStr.indexOf("<") > 0) {
            String[] types = typeStr.split("<|,|>");
            child.setGenericTypes(new ArrayList<>());

            for(int i = 0; i < types.length; ++i) {
                String type = types[i];

                try {
                    type = type.trim();
                    if (type.indexOf("<") > 0) {
                        type = type.substring(0, type.indexOf("<"));
                    }

                    if (!Void.TYPE.getCanonicalName().equals(type) && !type.startsWith("java.") && type.length() >= 3) {
                        BuilderJsonType genericType = new BuilderJsonType();
                        child.getGenericTypes().add(genericType);
                        genericType.setClassName(type);
                        Class genericClass = Class.forName(type);
                        scanParameter(genericClass, checkSet,child);
                    }else{
                        BuilderJsonType genericType = new BuilderJsonType();
                        processJavaType(Class.forName(type),genericType,checkSet);
                        child.getGenericTypes().add(genericType);
                    }
                } catch (Exception var9) {
                    logger.error(var9.getMessage(), var9);
                }
            }
        } else {
            try {
                if (Void.TYPE.getCanonicalName().equals(typeStr) || typeStr.startsWith("java.")) {
                    return;
                }

                Class genericClass = Class.forName(typeStr);
                scanParameter(genericClass, checkSet, child);
            } catch (Exception var10) {
                logger.error(var10.getMessage(), var10);
            }
        }

    }*/

    public static void getGenericField(Type type, Set<Class<?>> checkSet,BuilderJsonType child) {
        if(type instanceof ParameterizedType){
            child.setGenericTypes(new ArrayList<>());
            for (Type actualTypeArgument : ((ParameterizedType)type).getActualTypeArguments()) {
                BuilderJsonType jsonType = new BuilderJsonType();
                child.getGenericTypes().add(jsonType);
                processType(actualTypeArgument,checkSet,jsonType);
            }
        }else {
            try {


                Class genericClass = (Class) type;
                scanParameter(genericClass, checkSet, child);
            } catch (Exception var10) {
                logger.error(var10.getMessage(), var10);
            }
        }




    }
}
