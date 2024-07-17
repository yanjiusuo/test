package com.jd.workflow.flow.utils;

import com.jd.workflow.flow.core.exception.StepParseException;
import org.apache.camel.util.ObjectHelper;

public class ParserUtils {
    public static void notEmpty(Object value,String message){
        notEmpty(value,message,null,null);
    }
    public static void assertTrue(Boolean value,String message){
        if(value == null || value == false){
            StepParseException exception = new StepParseException(message);
            throw exception;
        }

    }
    public static void notEmpty(Object value,String message,String type,String id){
        if (ObjectHelper.isEmpty(value)) {
            StepParseException exception = new StepParseException(message).type(type).id(id);

            throw exception;
        }
    }
}
