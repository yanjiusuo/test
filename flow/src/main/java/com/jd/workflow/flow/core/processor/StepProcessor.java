package com.jd.workflow.flow.core.processor;


import com.jd.workflow.flow.core.metadata.StepMetadata;

import com.jd.workflow.flow.core.step.Step;

import java.lang.reflect.ParameterizedType;
import java.util.Map;

public   interface StepProcessor<T extends StepMetadata> {
    /**
     * 初始化参数
     * @param metadata 步骤参数，序列化为json以后的参数,初始化的时候才会被调用
     */

    public void init(T metadata);

    default public Class<T> getMetadataType(){
        return (Class<T>) ((ParameterizedType) getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
    }

    /**
     * 获取步骤类型，可以有多个，以，分割
     * @return
     */
    public  String getTypes();


    /**
     * 获取输入数据，调用process后执行
     * @return
     */
    //public Input getInput();


    /**
     *  步骤执行逻辑,必须设置 必须调用currentStep.setInput  及currentStep.setOutput方法
     * @param  currentStep
     * @param
     * @return
     */
    public void process(Step currentStep);




    default  public void stop() {
    }
}
