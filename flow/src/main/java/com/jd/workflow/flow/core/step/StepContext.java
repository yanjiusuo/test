package com.jd.workflow.flow.core.step;

import com.jd.workflow.flow.core.attr.AttributeSupport;
import com.jd.workflow.flow.core.definition.WorkflowParam;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.input.BaseInput;
import com.jd.workflow.flow.core.input.Input;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.BaseOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.ISubflowProcessor;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StepContext extends AttributeSupport {

    Map<String,Step> steps = new ConcurrentHashMap<>();
    Map<String,Object> attrs = new ConcurrentHashMap<>();
    Map<String,String> params = new HashMap<>();
    WorkflowInput input;
    Output output;
    boolean debugMode;
    StepExecException exception;
    ISubflowProcessor subflowProcessor;

    public Step getById(String stepId) {
        return steps.get(stepId);
    }
    public void registerStep(Step step){
        assert step !=null;
        assert !steps.containsKey(step.getId());
        steps.put(step.getId(),step);
        steps.put(step.getId(),step);
    }

    public void setSubflowProcessor(ISubflowProcessor subflowProcessor) {
        this.subflowProcessor = subflowProcessor;
    }

    public Map<String,Step> getSteps() {
        return steps;
    }

    @Override
    public void attr(String name, Object value) {
        attrs.put(name,value);
    }

    @Override
    public Object attr(String name) {
        return attrs.get(name);
    }


    public WorkflowInput getInput() {
        return input;
    }

    public void setInput(WorkflowInput input) {
        this.input = input;
    }

    public void setOutput(Output output) {
        this.output = output;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public Output getOutput() {
        return output;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public Output execSubflow(String flowId, Input input){
        if(subflowProcessor == null){
            throw new StepExecException(flowId,"flow.err_miss_subflow_executor").param("flowId",flowId).param("input",input);
        }
        return subflowProcessor.execSubflow(flowId,input);
    }

    public StepExecException getException() {
        return exception;
    }

    public void setException(StepExecException exception) {
        this.exception = exception;
    }

    public boolean isSuccess(){
        return this.exception == null;
    }

    /**
     * 生成日志信息，暂时与调试日志保持一致
     * @return
     */
    public Map<String,Object> toLog(){
        return toMap();
    }
    public Map<String,Object> toMap(){
        Map<String,Object> map = new LinkedHashMap<>();
        if(!attrs.isEmpty()){
            map.put("attrs",attrs);
        }
        map.put("input",input);
        map.put("output",output);
        map.put("params",params);
        if(exception != null && output != null){
            output.setException(exception);
        }
        if(isDebugMode()){
            if(input != null && !input.getVariables().isEmpty()){
                map.put("inputVariables",input.getVariables());
            }
            if(output != null
                    && output instanceof BaseOutput
                    && !((BaseOutput)output).getVariables().isEmpty()){
                map.put("outputVariables",((BaseOutput)output).getVariables());
            }
        }
        map.put("exception", ErrorMessageFormatter.formatMsg(exception));
        List stepsData =  new ArrayList();
        map.put("steps",stepsData);
        for (Map.Entry<String, Step> entry : steps.entrySet()) {
            Step step = entry.getValue();
            if(!StringUtils.isEmpty(step.getId()) && step.getId().startsWith("_")){// 内部步骤忽略掉
                continue;
            }
            if(step.invoked){
                Map<String, Object> stepMetadata = step.toMap();
                stepsData.add(stepMetadata);
                Input input = step.getInput();
                Output output = step.getOutput();
                if(input !=null
                        && input instanceof BaseInput
                        && !((BaseInput)input).getVariables().isEmpty()){
                    stepMetadata.put("inputVariables", ((BaseInput)input).getVariables());
                }
                if(output != null
                        && output instanceof BaseOutput
                        && !((BaseOutput)output).getVariables().isEmpty()){
                    stepMetadata.put("outputVariables", ((BaseOutput)output).getVariables());
                }
            }
        }

        return map;
    }

    public Map<String,Object> buildEnv(){
        Map<String,Object> args  = new HashMap<>();
        Map<String,Object> workflowParams = new HashMap<>();
        workflowParams.put("input",getInput());

        args.put("workflow",workflowParams);
        args.put("exception", exception);
        args.put("steps",steps);
        return args;
    }
}
