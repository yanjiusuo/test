package com.jd.workflow.flow.bean.utils;

import com.jd.workflow.flow.core.bean.IValidator;
import com.jd.workflow.flow.core.bean.annotation.FlowConfigParam;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class ValidateUtils {
    /**
     * 根据FlowConfigParam注解校验值的有效性
     * @param
     */
    public static void validateBeanStepInitArgs(String initClass,Object value){
        Map<String, FlowConfigParam> fieldAnnotations = new HashMap<>();
        try{
            Class<?> clazz = Class.forName(initClass);
            final Object classValue = JsonUtils.cast(value, clazz);
            String[] errors = validateInitValue(classValue);
            if(errors != null && errors.length > 0){
                throw new StepParseException(StringHelper.joinArray(errors,","));
            }
        }catch (ClassNotFoundException e) {
          throw StdException.adapt(e);
        }
    }

    private static String[] executeValidate(FlowConfigParam flowConfigParam,Object value,String prop){

        if(flowConfigParam == null) return new String[0];
        if(flowConfigParam.required() && ObjectHelper.isEmpty(value)){
            String label = flowConfigParam.label();
            if(StringHelper.isEmpty(label)){
                label = prop;
            }
            return new String[]{"必填参数不可为空:"+ label};
        }

        for (Class<? extends IValidator> aClass : flowConfigParam.validator()) {
            String[] errors = new String[0];
            try {
                errors = aClass.newInstance().validate(value);
            } catch (Exception e) {
                throw StdException.adapt(e);
            }
            if(errors != null && errors.length > 0){
                 return errors;
            }
        }
        return null;
    }

    public static String[] validateInitValue(Object value){
        Map<String, FlowConfigParam> fieldAnnotations = new HashMap<>();
        Class<?> clazz = value.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            FlowConfigParam flowConfigParam = field.getAnnotation(FlowConfigParam.class);
            if(flowConfigParam != null){
                fieldAnnotations.put(field.getName(),flowConfigParam);


                Object fieldValue = null;
                try {
                    fieldValue =   FieldUtils.readField(field,value,true);
                } catch (IllegalAccessException e) {
                    throw StdException.adapt(e);
                }
                String[] errors =  executeValidate(flowConfigParam,fieldValue,field.getName());
                if(errors != null && errors.length != 0){
                    return errors;
                }
            }

        }
        return null;
    }
}
