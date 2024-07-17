package com.jd.workflow.soap.common.parser.impl;

import com.jd.workflow.soap.common.parser.TypeConverter;
import com.jd.workflow.soap.common.parser.TypeConverterRegistry;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GenericArrayTypeConverter implements TypeConverter {
    @Override
    public boolean match(Type type) {
        return type instanceof GenericArrayType;
    }

    @Override
    public void convert(Type type, Set<Class<?>> checkSet, BuilderJsonType jsonType, Map<String,Type> boundTypeVariable) {
        GenericArrayType arrayType = (GenericArrayType) type;

        BuilderJsonType currentType = jsonType;

        jsonType.setType("array");

        GenericArrayType compsType = (GenericArrayType)type;
        Type realType = compsType.getGenericComponentType();
        do{
            BuilderJsonType child = new BuilderJsonType();
            child.setName("$$0");
            currentType.addChild(child);
            currentType  = child;
            if(realType instanceof GenericArrayType){
                currentType.setType("array");
                realType = ((GenericArrayType)realType).getGenericComponentType();

            }else{
                break;
            }

        }while (true);


        TypeConverterRegistry.processJsonType(realType,checkSet,currentType,boundTypeVariable);


    }

    /*public static void main(String[] args) {
         GenericArrayTypeConverter genericArrayTypeConverter = new GenericArrayTypeConverter();
         Demo<JsonType> demo = new Demo<>();
        BuilderJsonType builderJsonType = new BuilderJsonType();
        HashMap<String,Type> map = new HashMap<>();
        map.put("T",JsonType.class);
        genericArrayTypeConverter.convert(demo.getClass().getDeclaredFields()[0].getGenericType(),new HashSet<>(), builderJsonType,map);
        System.out.println(JsonUtils.toJSONString(builderJsonType));
    }
    private  void test(Demo<JsonType> demo){

    }
    public static class Demo<T>{
        T[] t;
        T[][] tt;

        public T[] getT() {
            return t;
        }

        public void setT(T[] t) {
            this.t = t;
        }

        public T[][] getTt() {
            return tt;
        }

        public void setTt(T[][] tt) {
            this.tt = tt;
        }
    }*/
}
