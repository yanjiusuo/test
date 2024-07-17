package com.jd.workflow.flow.utils;

import com.jd.workflow.flow.core.attr.AttributeSupport;
import com.jd.workflow.flow.core.expr.IEvalVarCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EvalContextVars {
    private static final ThreadLocal<Map<String,Object>> LOCAL = new ThreadLocal<>();
    private static final ThreadLocal<IEvalVarCollector> varCollector = new ThreadLocal<>();
    /**
     * 设置用户信息
     */
    public static void setVars(Map<String,Object> vars) {
        LOCAL.set(vars);
    }

    public static void makeStepVarCollector(AttributeSupport step,
                                            String stepId){
        EvalContextVars.setVarCollector(new IEvalVarCollector() {
            @Override
            public void collect(Map<String, Object> variables) {
                List<Map<String,Object>> savedVars = (List<Map<String,Object>>) step.getVariables().computeIfAbsent("vars_"+stepId, vs->{
                    return new ArrayList<>();
                });
                if(!variables.isEmpty()){
                    savedVars.add(variables);
                }

            }
        });
    }

    /**
     * 获取登录用户信息
     */
    public static Map<String,Object> getVars() {
        return LOCAL.get();
    }

    /**
     * 获取登录用户信息
     */
    public static void removeVars() {
         LOCAL.remove();
    }

    /**
     * 设置用户信息
     */
    public static void setVarCollector(IEvalVarCollector collector) {
        varCollector.set(collector);
    }

    /**
     * 获取登录用户信息
     */
    public static IEvalVarCollector getVarCollector() {
        return varCollector.get();
    }

    /**
     * 获取登录用户信息
     */
    public static void removeCollector() {
        varCollector.remove();
    }
}
