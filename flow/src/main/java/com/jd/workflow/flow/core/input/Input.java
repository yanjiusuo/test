package com.jd.workflow.flow.core.input;

import com.jd.workflow.flow.core.attr.AttributeSupport;
import com.jd.workflow.flow.core.exception.StepValidateException;

public interface Input  {

    public default void error(String message){
        throw new StepValidateException(message);
    }

    public  void attr(String name, Object value) ;

    public Object attr(String name) ;
}
