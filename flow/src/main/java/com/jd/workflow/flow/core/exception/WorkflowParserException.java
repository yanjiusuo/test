package com.jd.workflow.flow.core.exception;

import com.jd.workflow.soap.common.exception.StdException;

public class WorkflowParserException extends StdException {


    public WorkflowParserException( String message) {
        this(message,null);
    }
    public WorkflowParserException( String message,Throwable throwable) {
        super(message,throwable);
    }



}
