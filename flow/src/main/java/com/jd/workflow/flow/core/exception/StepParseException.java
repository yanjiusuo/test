package com.jd.workflow.flow.core.exception;

import com.jd.workflow.soap.common.exception.StdException;

import java.util.HashMap;
import java.util.Map;

public class StepParseException extends StdException {
    String type;

    public StepParseException( String message) {
        this(message,null);
    }
    public StepParseException( String message,Throwable throwable) {
        super(message,throwable);
    }


    public String getId() {
        return (String)params.get("id");
    }

    public StepParseException id(String id) {

        param("id",id);
        return this;
    }
    public StepParseException type(String type) {
        this.type = type;
        param("type",type);
        return this;
    }
}
