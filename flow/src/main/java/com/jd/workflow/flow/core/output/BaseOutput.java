package com.jd.workflow.flow.core.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.flow.core.attr.AttributeSupport;


public  abstract class BaseOutput extends AttributeSupport implements Output {
    @JsonSerialize(using = ExceptionSerializer.class)
    Exception exception;
    boolean success=true;
    Object body;
    Long logId;

    public Exception getException(){
        return this.exception;
    }
    public  void setException(Exception exception){
        this.exception =exception;
        success = false;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }
}
