package com.jd.workflow.flow.core.exception;

import com.jd.workflow.soap.common.exception.StdException;
import org.apache.camel.Exchange;

import java.util.HashMap;
import java.util.Map;

public class StepExecException extends StdException {

    Exchange exchange = null;
    public StepExecException(String stepId, String message) {
       this(stepId,message,null);
    }

    public StepExecException(String stepId, String message, Throwable throwable) {
        super(message,throwable);
        param("stepId",stepId);
    }

    public Exchange getExchange() {
        return exchange;
    }

    public void setExchange(Exchange exchange) {
        this.exchange = exchange;
    }

    public String getStepId() {
        return (String) params.get("stepId");
    }



    public void setStepId(String stepId) {
        param("stepId",stepId);
    }
    public String getOriginalMessage(){
        return super.getMessage();
    }

    /**
     * workflow执行时，需要执行mvel脚本，这时候需要
     * @return
     */
   @Override
    public String getMessage() {
        return ErrorMessageFormatter.formatMsg(this);
    }
}
