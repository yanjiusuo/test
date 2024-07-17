package com.jd.workflow.flow.core.exception;

public class StepTimeoutException extends StepExecException {

    public StepTimeoutException(String stepId, String message) {
        super(stepId, message);
    }
}
