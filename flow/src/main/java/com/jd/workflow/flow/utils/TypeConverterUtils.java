package com.jd.workflow.flow.utils;

import com.jd.workflow.flow.core.exception.TypeCastException;
import com.jd.workflow.soap.common.util.JsonUtils;

public class TypeConverterUtils {
    public static <T> T cast(Object obj,Class<T> clazz,String stepId){
        try{
            return JsonUtils.cast(obj,clazz);
        }catch (Exception e){
            TypeCastException exception = new TypeCastException(e);
            exception.id(stepId);
            throw exception;
        }

    }
}
