package com.jd.workflow.flow.utils;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.soap.common.mapping.CommonParamMappingUtils;

import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.Map;

/**
 * 映射上下文，同一个服务多次映射可以避免多次够着evalContext上下文
 */
public class ParamMappingContext {
    public ParamMappingContext(StepContext stepContext){
        this(stepContext,null);
    }
    public ParamMappingContext(StepContext stepContext,Map<String,Object> extArgs){
        this.stepContext = stepContext;
        this.extArgs = extArgs;
    }
    // 执行上下文
    StepContext stepContext;
    // 额外的参数,stepContext里的上下文变量已经固定了，假如需要额外的参数的话放到extArgs里就行了
    Map<String,Object> extArgs;
    CommonParamMappingUtils.EvalContext evalContext;
    public CommonParamMappingUtils.EvalContext getEvalContext(){

        if(evalContext == null){
            ParametersUtils utils = new ParametersUtils();
            Map<String, Object> inputMap = utils.getStepExecVars(stepContext);
            Map<String, Object> jsonMap = utils.getJsonEvalVars(stepContext);
            if(!CollectionUtils.isEmpty(extArgs)){
                inputMap.putAll(extArgs);
                jsonMap.putAll(extArgs);
            }
            Configuration option =
                    Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
            DocumentContext documentContext = JsonPath.parse(jsonMap, option);

            CommonParamMappingUtils.EvalContext evalContext = new CommonParamMappingUtils.EvalContext();
            evalContext.setArgs(inputMap);
            evalContext.setDocumentContext(documentContext);
            this.evalContext = evalContext;
        }
        return evalContext;
    }

    public StepContext getStepContext() {
        return stepContext;
    }
}
