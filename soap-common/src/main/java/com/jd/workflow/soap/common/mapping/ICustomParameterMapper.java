package com.jd.workflow.soap.common.mapping;

import java.util.Map;

/**
 * 正常情况下，需要将json表达式映射为具体的值，比如：
  {
    a:${workflow.input.params.a}
    b:1
  }
  可以看到,值目前支持json expr、常量，如果需要扩展怎么办呢？ 可以通过ICustomParameterMapper来做。
  1. 实现一个ICustomParameterMapper类
  2. 实现 JsonType的getExprValue方法，返回一个ICustomParameterMapper实例
 */
public interface ICustomParameterMapper {
    public Object evaluate(CommonParamMappingUtils utils, CommonParamMappingUtils.EvalContext context);
}
