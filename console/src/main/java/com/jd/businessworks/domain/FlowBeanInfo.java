package com.jd.businessworks.domain;

import com.jd.workflow.soap.common.method.MethodMetadata;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 解析java bean得到的bean列表
 */
@Data
public class FlowBeanInfo {
    public static Integer BEAN_TYPE_UTILS = 1;
    public static Integer BEAN_TYPE_SERVICE = 2;
    /**
     * bean名称
     */
    String beanName;
    /**
     * 类名
     */
    String fullClassName;
    Object bean;
    /**
     * 类描述
     */
    String desc;
    /**
     * bean类型：1-工具类 2-服务类
     */
    Integer serviceType;
    /**
     * bean方法列表
     */
    List<MethodMetadata> methods;
}