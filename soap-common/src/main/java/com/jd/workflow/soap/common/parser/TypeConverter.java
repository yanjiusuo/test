package com.jd.workflow.soap.common.parser;

import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * jsf泛化调用
 */
public interface TypeConverter {
    public boolean match(Type type);
    default public void convert(Type type, Set<Class<?>> checkSet,BuilderJsonType currentType){
        convert(type, checkSet, currentType, Collections.emptyMap());
    }

    /**
     * 将type转换为currentType
     * @param type 要转换的类型
     * @param checkSet 已经处理过的类型，用来标记死循环
     * @param currentType 当前类型
     * @param boundTypeVariable type对应的参数化类型，比如针对IEntityMapper<T> ,若实例化为IEntityMapper<Person>，则需要将T替换为Person类型，对应的boundTypeVariable为：{T:Person}
     */
    public void convert(Type type, Set<Class<?>> checkSet, BuilderJsonType currentType, Map<String,Type> boundTypeVariable);
}
