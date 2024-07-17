package com.jd.workflow.flow.core.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.flow.core.attr.AttributeSupport;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Output  {

    public Exception getException();
    public  void setException(Exception exception);

    public boolean isSuccess() ;

    public void setSuccess(boolean success);

    public Object getBody();

    public void setBody(Object body);

    public  void attr(String name, Object value) ;

    public Object attr(String name) ;
}
