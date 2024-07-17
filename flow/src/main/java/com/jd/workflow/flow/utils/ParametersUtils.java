package com.jd.workflow.flow.utils;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.soap.common.mapping.CommonParamMappingUtils;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import org.springframework.util.CollectionUtils;

/*
 {
   input:{
     headers:map,
     body:map,
   },
   steps:{
      step1:{
        input:xx,
        output:xx
      }
   }}
 }
 */
public class ParametersUtils extends CommonParamMappingUtils {


    private final TypeReference<Map<String, Object>> map = new TypeReference<Map<String, Object>>() {};
    public ParametersUtils(){

    }

    public Map<String, Object> getJsonInputValue(
            List<?  extends JsonType> inputParams,
            StepContext context
            //TaskDef taskDefinition,
    ) {
        return getJsonInputValue(inputParams,new ParamMappingContext(context,null));
    }
    public Map<String, Object> getJsonInputValue(
            List<?  extends JsonType> inputParams,
            ParamMappingContext context
            //TaskDef taskDefinition,
    ) {
        if(inputParams == null || inputParams.isEmpty()) return new HashMap();
        Map<String,Object> args = new HashMap<>();
        for (JsonType inputParam : inputParams) {

            args.put(inputParam.getName(),inputParam.toExprValue());
        }
        Map<String, Object> value = getTaskInput(args, context);
        ObjectJsonType parentJsonType = new ObjectJsonType();
        parentJsonType.getChildren().addAll(inputParams);
        return (Map<String, Object>) parentJsonType.castValue(value);
    }
    /**
     * 构造value映射模板
     * @param inputParams
     * @return
     */
    public Map<String,Object> buildInput( List<?  extends JsonType> inputParams){
        if(inputParams == null
         || inputParams.isEmpty()
        ) return null;
        Map<String,Object> args = new HashMap<>();
        for (JsonType inputParam : inputParams) {

            args.put(inputParam.getName(),inputParam.toExprValue());
        }
        return args;
    }
    public Object getJsonInputValue(
            JsonType input,
            StepContext context
            //TaskDef taskDefinition,
    ){
        return getJsonInputValue(input,new ParamMappingContext(context));
    }
    /* public Object getJsonInputValue(
            JsonType input,
            StepContext context
            //TaskDef taskDefinition,
    ) {
        Map<String,Object> args = new HashMap<>();
        args.put("input",input.toExprValue());

        Map<String, Object> result = getTaskInput(args, context);
        return result.get("input");
    }*/
    public Object getJsonInputValue(
            JsonType input,
            ParamMappingContext context
            //TaskDef taskDefinition,
    ) {
        if(input == null) return null;
        Map<String,Object> args = new HashMap<>();
        args.put("input",input.toExprValue());

        Map<String, Object> result = getTaskInput(args, context);
        return input.castValue(result.get("input"));
    }

    public Map<String, Object> getTaskInput(
            Map<String, Object> inputParams,
            StepContext context
            //TaskDef taskDefinition,
       ) {
        return getTaskInput(inputParams,context,null);
    }

    public Map<String, Object> getTaskInput(
            Map<String, Object> input,
            StepContext context,
            Map<String,Object> extArgs
    ) {
        ParamMappingContext paramMappingContext = new ParamMappingContext(context,extArgs);
        return getTaskInput(input,paramMappingContext);
    }
    public Map<String, Object> getTaskInput(
            Map<String, Object> input,
            ParamMappingContext context
    ) {
        Map<String, Object> inputParams;

        if (input != null) {
            inputParams = clone(input);
        } else {
            inputParams = new HashMap<>();
        }
      /*  if (taskDefinition != null && taskDefinition.getInputTemplate() != null) {
            clone(taskDefinition.getInputTemplate()).forEach(inputParams::putIfAbsent);
        }*/


        EvalContext evalContext = context.getEvalContext();


        Map<String, Object> replacedTaskInput = replace(inputParams, evalContext);

        return replacedTaskInput;
    }
    public String evalJsonExpr(String jsonPath,Map<String,Object> inputMap){

        Configuration option =
                Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
        DocumentContext documentContext = JsonPath.parse(inputMap, option);

        return (String) replaceVariables(jsonPath,documentContext);
    }
    public Map<String,Object> getJsonEvalVars(StepContext context){
        Map<String, Object> inputMap = new HashMap<>();

        Map<String, Object> workflowParams = new HashMap<>();
        workflowParams.put("input", context.getInput() == null ? new HashMap<>() : context.getInput().toInputMap());
        workflowParams.put("output", context.getOutput());
        workflowParams.put("attrs", context.getAttrs());
        workflowParams.put("params",context.getParams());
        Map<String,Object> exceptionMap = new HashMap<>();
        if(context.getException() != null){
            exceptionMap.put("message", ErrorMessageFormatter.formatMsg(context.getException()));
            exceptionMap.put("params",context.getException().getParams());
            exceptionMap.put("stepId",context.getException().getStepId());
            exceptionMap.put("msg",context.getException().getMsg());
        }
        workflowParams.put("exception", exceptionMap);


        inputMap.put("context", workflowParams);
        inputMap.put("workflow", workflowParams);

        //inputMap.put("context", context);

        // For new context being started the list of tasks will be empty

        Map<String,Object> steps = new HashMap<>();
        for (Entry<String, Step> entry : context.getSteps().entrySet()) {
            Step task = entry.getValue();
            Map<String, Object> taskParams = new HashMap<>();
            taskParams.put("input", BeanTool.toMap(task.getInput()));
            taskParams.put("output", BeanTool.toMap(task.getOutput()));
            taskParams.put("attrs", task.getAttrs());

            steps.put(
                    task.getId(),
                    taskParams);
        }

        inputMap.put("steps",steps);
        return inputMap;
    }
    public Map<String, Object> getMvelExecVars(StepContext context){

        Map<String, Object> workflowParams = new HashMap<>();
        workflowParams.put("workflow", context);
        workflowParams.put("steps", context.getSteps());
        return workflowParams;
    }
    public Map<String, Object> getStepExecVars(StepContext context) {
        Map<String, Object> inputMap = new HashMap<>();

        Map<String, Object> workflowParams = new HashMap<>();
        workflowParams.put("input", context.getInput() == null ? new HashMap<>() : context.getInput().toInputMap());
        workflowParams.put("output", context.getOutput());
        workflowParams.put("params",context.getParams());
        /*workflowParams.put("status", context.getStatus());
        workflowParams.put("workflowId", context.getWorkflowId());
        workflowParams.put("parentWorkflowId", context.getParentWorkflowId());
        workflowParams.put("parentWorkflowTaskId", context.getParentWorkflowTaskId());
        workflowParams.put("workflowType", context.getWorkflowName());
        workflowParams.put("version", context.getWorkflowVersion());
        workflowParams.put("correlationId", context.getCorrelationId());
        workflowParams.put("reasonForIncompletion", context.getReasonForIncompletion());
        workflowParams.put("schemaVersion", context.getWorkflowDefinition().getSchemaVersion());
        workflowParams.put("variables", context.getVariables());*/

        inputMap.put("context", workflowParams);
        inputMap.put("workflow", workflowParams);

        //inputMap.put("context", context);

        // For new context being started the list of tasks will be empty

        Map<String,Object> steps = new HashMap<>();
        for (Entry<String, Step> entry : context.getSteps().entrySet()) {
            Step task = entry.getValue();
            Map<String, Object> taskParams = new HashMap<>();
            taskParams.put("input", task.getInput());
            taskParams.put("output", task.getOutput());
            taskParams.put("attrs", task.getAttrs());
                            /*taskParams.put("taskType", task.getTaskType());
                            if (task.getStatus() != null) {
                                taskParams.put("status", task.getStatus().toString());
                            }
                            taskParams.put("referenceTaskName", task.getReferenceTaskName());
                            taskParams.put("retryCount", task.getRetryCount());
                            taskParams.put("correlationId", task.getCorrelationId());
                            taskParams.put("pollCount", task.getPollCount());
                            taskParams.put("taskDefName", task.getTaskDefName());
                            taskParams.put("scheduledTime", task.getScheduledTime());
                            taskParams.put("startTime", task.getStartTime());
                            taskParams.put("endTime", task.getEndTime());
                            taskParams.put("workflowInstanceId", task.getWorkflowInstanceId());
                            taskParams.put("taskId", task.getTaskId());
                            taskParams.put(
                                    "reasonForIncompletion", task.getReasonForIncompletion());
                            taskParams.put("callbackAfterSeconds", task.getCallbackAfterSeconds());
                            taskParams.put("workerId", task.getWorkerId());*/
            steps.put(
                    task.getId(),
                    taskParams);
        }

        inputMap.put("steps",steps);
        return inputMap;
    }




/*    public Map<String, Object> getWorkflowInput(
            WorkflowDef workflowDef, Map<String, Object> inputParams) {
        if (workflowDef != null && workflowDef.getInputTemplate() != null) {
            clone(workflowDef.getInputTemplate()).forEach(inputParams::putIfAbsent);
        }
        return inputParams;
    }*/
}
