package com.jd.workflow.flow.core.expr;

/**
 * 用来查找mvel表达式里用到的变量
 */
public interface VariableRegistry {
    public Object resolve(String varName);
}
