package com.jd.workflow.flow.core.camel;



import com.jd.workflow.flow.core.context.BeanRegistry;
import com.jd.workflow.flow.core.context.FlowExecContext;
import com.jd.workflow.flow.core.context.FlowContextAware;
import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Environment;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;

import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.utils.StepContextHelper;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.*;
import org.apache.camel.support.DefaultExchange;
import org.apache.camel.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.jd.workflow.flow.core.camel.RouteBuilder.STEP_SEPARATOR;

public class CamelStepBean implements Processor,Service {
    static final Logger logger = LoggerFactory.getLogger(CamelStepBean.class);
    StepProcessor stepProcessor;
    StepMetadata stepMetadata;
    CamelContext camelContext;
    public CamelStepBean(){
    }
    public void setCamelContext(CamelContext camelContext){
        this.camelContext = camelContext;
    }
    public void init(BeanDefinition definition) throws IllegalAccessException, InstantiationException {

        String defText = definition.getDescriptionText();

        Object txtObj = JsonUtils.parse(defText);
        if(txtObj == null){
            throw new StepParseException("workflow.err_step_miss_description");
        }
        Map<String,Object> args = (Map<String, Object>) txtObj;
        StepMetadata stepMetadata =StepProcessorRegistry.parseMetadata(args);

        this.stepProcessor = StepProcessorRegistry.instance((String) args.get("type"),null);
        if(this.stepProcessor instanceof FlowContextAware){
            FlowExecContext flowContext = new FlowExecContext();
            flowContext.setBeanRegistry(new BeanRegistry(){

                @Override
                public Object get(String name) {
                    return camelContext.getRegistry().lookupByName(name);
                }
            });
            ((FlowContextAware)stepProcessor).setFlowContext(flowContext);
        }
        this.stepProcessor.init(stepMetadata);
        this.stepMetadata = stepMetadata;
    }

    /**
     *  multicast步骤可能抛出 CamelExchangeException ，camelExchangeException异常不具有参考价值，需要找到原始的错误
     * @param e
     * @return
     */
    StepExecException getCauseExecException(Throwable e){
        if(e == null ) return null;
        if(e instanceof StepExecException){
            return (StepExecException) e;
        }
        return getCauseExecException(e.getCause());
    }
    @Override
    public void process(Exchange o) throws Exception {
        DefaultExchange exchange = (DefaultExchange)o;
        String stepId =  exchange.getHistoryNodeId();
        if(stepId.indexOf(STEP_SEPARATOR) != -1){
            stepId = stepId.substring(stepId.lastIndexOf(":")+1);
        }
        StepContext stepContext = StepContextHelper.getStepContext(exchange);
        Exception execException = (Exception) exchange.getProperty(ExchangePropertyKey.EXCEPTION_CAUGHT);

        if(execException!= null ){
            StepExecException e = null;
            if(!(execException instanceof StepExecException)){
                 e = getCauseExecException(execException);
            }else {
                e = (StepExecException) execException;
            }
            if(e == null){
                logger.error("camel.found_unexpected_exception",execException);

                StepExecException exception = new StepExecException(stepId,"step.err_exec_step",execException);
                exception.param("message",execException.getMessage());
                exception.setStepId(stepId);
                stepContext.setException(exception);
            }else{
                stepContext.setException(e);
            }

        }
        Step currentStep = new Step();
        currentStep.setInvoked(true);
        currentStep.setId(stepId);
        if(stepMetadata != null){
            currentStep.setKey(stepMetadata.getKey());
        }
        currentStep.setContext(stepContext);
        stepContext.registerStep(currentStep);
        StopWatch stopWatch = new StopWatch();
        try {

            stepProcessor.process(currentStep);
            if(currentStep.getInput() == null){
                //throw new StepExecException(stepId,"workflow.err_input_must_be_set");
            }
            if(currentStep.getOutput() == null){
                //throw new StepExecException(stepId,"workflow.err_output_must_be_set");
            }
            currentStep.setCostTime(stopWatch.taken());

            stepContext.setOutput(currentStep.getOutput());
            exchange.getOut().setBody(currentStep.getOutput());
            TaskDefinition taskDef = stepMetadata.getTaskDef();
            if(taskDef != null && taskDef.getDelayTime() != null){
                try {
                    Thread.sleep(taskDef.getDelayTime());
                } catch (InterruptedException e) {
                    throw new StepExecException(stepMetadata.getId(),"step.err_delay_is_interrupted",e);
                }
            }

        }catch (Exception e){
            logger.error("camel.err_exec_step",e);
            currentStep.setSuccess(false);
            currentStep.setCostTime(stopWatch.taken());
            if(e instanceof StepExecException){
                stepContext.setException((StepExecException) e);
                throw e;
            }
            StepExecException exception = new StepExecException(stepId,e.getMessage(),e);
            exception.setExchange(exchange);
            stepContext.setException(exception);
            throw exception;
        }

    }


    @Override
    public void start() {

    }

    @Override
    public void stop() {
        stepProcessor.stop();
    }
}
