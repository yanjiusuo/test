package com.jd.workflow.flow.core.expr;

import java.util.Map;

/**
 * 执行时变量收集器，可用将mvel执行时的临时变量收集起来
 */
public interface IEvalVarCollector {
    public void collect(Map<String,Object> variables);
}
