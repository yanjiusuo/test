package com.jd.workflow.flow.core.exception;

public class StepRespValidateException extends StepExecException {

    public StepRespValidateException(String stepId, String message) {
        super(stepId, message);
    }
}
