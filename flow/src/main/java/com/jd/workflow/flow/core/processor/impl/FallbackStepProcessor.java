package com.jd.workflow.flow.core.processor.impl;

import com.jd.workflow.flow.core.context.FlowContextAware;
import com.jd.workflow.flow.core.context.FlowExecContext;
import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.exception.StepScriptEvalException;
import com.jd.workflow.flow.core.input.Input;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;

import com.jd.workflow.flow.core.output.BaseOutput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.core.retry.ExecContext;
import com.jd.workflow.flow.core.retry.RetryConfig;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.http.HttpStatus;

import java.util.Map;

public class FallbackStepProcessor implements StepProcessor<FallbackStepMetadata> , FlowContextAware {
    StepProcessor stepProcessor;
    FallbackStepMetadata metadata;
    FlowExecContext flowExecContext;
    @Override
    public void setFlowContext(FlowExecContext flowContext) {
        this.flowExecContext = flowContext;
    }
    @Override
    public void init(FallbackStepMetadata metadata) {
        String type = metadata.getType();
        this.stepProcessor = StepProcessorRegistry.instance(type,flowExecContext,false);

        this.stepProcessor.init(metadata);
        this.metadata = metadata;
    }

    @Override
    public String getTypes() {
        return "fallback";
    }

    @Override
    public void process(Step currentStep) {
        TaskDefinition taskDef = metadata.getTaskDef();

        TaskDefinition.Fallback fallback = null;
        TaskDefinition.FallbackStrategy fallbackStrategy = null;
        if(taskDef != null){
            fallback = taskDef.getFallback();
            fallbackStrategy = taskDef.getFallbackStrategy();
        }
        StepExecException exception = null;

        try{
            this.stepProcessor.process(currentStep);
            Output output = currentStep.getOutput();
            Input input = currentStep.getInput();
            if( metadata.getSuccessCondition()!= null){
                ParametersUtils utils = new ParametersUtils();
                Map<String, Object> stepExecVars = utils.getStepExecVars(currentStep.getContext());
                stepExecVars.put("input",input);
                stepExecVars.put("output",output);
                Object evaluate = null;
                if(output instanceof BaseOutput){
                    evaluate = MvelUtils.eval(metadata.getId(),"successCondition",metadata.getSuccessCondition(),stepExecVars,(BaseOutput)output);
                }else{
                    evaluate = MvelUtils.eval(metadata.getId(),"successCondition",metadata.getSuccessCondition(),stepExecVars,null);
                }

                Boolean val = Variant.valueOf(evaluate).toBool(null);
                if(evaluate == null || val == false){ // 未成功
                    exception = new StepExecException(metadata.getId(),"httpstep.err_exec_success_condition");
                    exception.param("body", JsonUtils.toJSONString(output.getBody()));
                }
            }
        }catch(StepScriptEvalException e){// 步骤里写的有错误的话不予执行
            throw e;
        }catch (StepExecException e){
            exception = e;
        }catch (Exception e){
            exception =  new StepExecException(metadata.getId(),e.getMessage(),e);
        }
        Output output = currentStep.getOutput();
        if(exception != null){
            if(output != null){
                output.setException(exception);
                output.setSuccess(false);
                if(TaskDefinition.FallbackStrategy.CONTINUE.equals(fallbackStrategy) ){
                    if(fallback != null){
                        output.setBody(fallback.getValue());
                    }
                    if(output instanceof HttpOutput){
                        ((HttpOutput) output).setStatus(HttpStatus.SC_OK);
                    }
                    output.attr("__exceptionReason",exception.getMessage());
                    return;
                }else if(TaskDefinition.FallbackStrategy.RETRY.equals(fallbackStrategy)){
                    processRetry(currentStep,exception);
                }
            }

            throw exception;
        }
    }
    private void processRetry(Step currentStep,StepExecException exception){
        RetryConfig retryConfig = metadata.getTaskDef().getRetryConfig();
        ExecContext ctx = currentStep.getExecContext();
        if(ctx == null){
            ctx = new ExecContext();
            ctx.setRetryCount(0);
            ctx.setGetLastThrowable(exception);
            ctx.setRetryStartTime(System.currentTimeMillis());
            currentStep.setExecContext(ctx);
        }else{
            ctx.setRetryCount(ctx.getRetryCount()+1);
        }
        if(retryConfig.getExecuteInterval() != null){
            int nextTime = retryConfig.getExecuteInterval().getNextInterval(ctx);
            if(nextTime > 0){
                try {
                    Thread.sleep(nextTime);
                } catch (InterruptedException e) {
                    throw new StepExecException(metadata.getId(),"step.err_delay_is_interrupted",e);
                }
            }

        }
        if(retryConfig.getRetryPolicy() != null){
            boolean toBeContinue = retryConfig.getRetryPolicy().toBeContinue(ctx);
            if(toBeContinue){
                currentStep.setInput(null);
                currentStep.setOutput(null);
                process(currentStep);
            }
        }
    }
}
