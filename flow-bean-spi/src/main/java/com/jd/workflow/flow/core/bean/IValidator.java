package com.jd.workflow.flow.core.bean;

public interface IValidator<T> {
    /**
     * 校验值指定属性的有效性
     * @return 错误不为空返回空对象或者空数组，否则返回值
     */
    public String[] validate(T value);
}
