package com.jd.workflow.jsf.cast;

import com.jd.workflow.soap.common.xml.schema.JsonType;

/**
 * 可以参考下com.jd.org.msgpack.template.TemplateRegistry
 * 这里面包含了所有常见的java序列化方式
 */
public interface JsfParamConverter<T> {
    /**
     * 将value类型的值转换为对应的jsf泛化调用参数
     * @param
     * @param value
     * @return
     */
    public Object write( JsonType currentJsonType, Object value);

    /**
     * 将value转换为T类型，用来做类型转换
     * @param value
     * @return
     */
    default public T read( Object value){
        return null;
    }

    default public Object getDemoValue(Class type){return null;}
}