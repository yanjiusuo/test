package com.jd.workflow.flow.core.bean;

import com.jd.workflow.flow.core.bean.annotation.FlowMethod;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 分为从jar包加载及从初始化参数加载
 *
 */
public interface IBeanStepProcessor<T,C> {

    /**
     * 获取步骤名称
     * @return
     */
    public String getName();

    /**
     * 用来初始化java bean,若在内存里的话，该属性可以被忽略
     * @return
     */
    public IBeanFactory<T,C> getBeanFactory();

    /**
     * 获取初始化参数
     * @return
     */
    public Class<C> getInitConfigClass();

    /**
     * 获取bean类型
     * @return
     */
    public Class<T> getBeanType();

    /**
     * 获取要暴露的所有方法名称,默认为所有的公开方法
     * @return
     */
    default public List<Method> getExportMethods(){
        List<Method> methods = new ArrayList<>();
        for (Method method : getBeanType().getMethods()) {
            if(method.getAnnotation(FlowMethod.class) != null){
                methods.add(method);
            }
        }
        return methods;
    }
}
