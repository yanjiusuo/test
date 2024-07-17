package com.jd.workflow.flow.utils;

import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.step.Environment;
import com.jd.workflow.flow.core.step.StepContext;
import org.apache.camel.Exchange;

public class StepContextHelper {
    static final String STEP_CONTEXT_NAME = "stepContext";
    static final String ENVIRONMENT_NAME = "environment";
    static final String PREV_STEP_NAME = "prevStepId";
    public static StepContext getStepContext(Exchange exchange){
        return (StepContext)exchange.getProperty(STEP_CONTEXT_NAME);
    }
    public static void setStepContext(Exchange exchange,StepContext stepContext){
        exchange.setProperty(STEP_CONTEXT_NAME,stepContext);
    }
    public static StepContext setInput(Exchange exchange, WorkflowInput workflowInput){
        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);

        exchange.setProperty(STEP_CONTEXT_NAME,stepContext);
        return stepContext;
    }
    public static StepContext getInput(Exchange exchange){
        return (StepContext) exchange.getProperty(STEP_CONTEXT_NAME);
    }
    public static Environment getEnvironment(Exchange exchange){
        return (Environment)exchange.getProperty(ENVIRONMENT_NAME);
    }
    public static String getPrevStepId(Exchange exchange){
        return (String)exchange.getProperty(PREV_STEP_NAME);
    }

    public static void setPrevStepId(Exchange exchange,String stepId){
        exchange.setProperty(PREV_STEP_NAME,stepId);
    }
}
