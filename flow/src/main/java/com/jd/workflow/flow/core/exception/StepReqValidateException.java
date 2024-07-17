package com.jd.workflow.flow.core.exception;

import com.jd.workflow.soap.common.exception.StdException;
import org.apache.camel.Exchange;

public class StepReqValidateException extends StepExecException {

    public StepReqValidateException(String stepId, String message) {
        super(stepId, message);
    }
}
