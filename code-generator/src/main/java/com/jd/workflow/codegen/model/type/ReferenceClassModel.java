package com.jd.workflow.codegen.model.type;

import com.jd.workflow.soap.common.util.TypeUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * java的基础引用类型
 */
@Getter
@Setter
public class ReferenceClassModel extends IClassModel {


    @Override
    public IClassModel clone() {
        ReferenceClassModel classModel = new ReferenceClassModel();
        classModel.setClassName(getClassName());
        classModel.setFormalParams(new ArrayList<>(formalParams));
        classModel.setGenericTypes(new ArrayList<>(genericTypes));
        return classModel;
    }

    @Override
    public boolean isArray() {
        return  false;
    }

    @Override
    public boolean isObject() {
        return false;
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }


    private static boolean isException(String className){
        try{
            Class<?> aClass = Class.forName(className);
            return Throwable.class.isAssignableFrom(aClass)  || Error.class.isAssignableFrom(aClass);
        }catch (Exception e){}
        return false;
    }
    private static boolean isMap(String className){
        try{
            Class<?> aClass = Class.forName(className);
            return Map.class.isAssignableFrom(aClass) ;
        }catch (Exception e){}
        return false;
    }
    @Override
    public String getJsType() {
        String simpleType = TypeUtils.getSimpleType(getClassName());
        if(simpleType != null){
            return simpleType;
        }else if(isException(getClassName())){
            return "string";
        }else if(isMap(getClassName())){
            if(getGenericTypes().isEmpty()){
                return "Map<any,any>";
            }else{
                String sb = "Map<";
                sb+= getGenericTypes().stream().map(vs->vs.getJsType()).collect(Collectors.joining(","));
                sb+=">";
                return sb;
            }
        }else{
            return "any";
        }
    }
}
