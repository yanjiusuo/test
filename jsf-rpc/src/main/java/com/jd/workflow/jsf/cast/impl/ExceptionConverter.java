package com.jd.workflow.jsf.cast.impl;


import com.alibaba.fastjson.JSONObject;
import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;

public class ExceptionConverter implements JsfParamConverter {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        //throw new TypeConvertException("typeconvert.err_exception_param_not_support").param("class",currentJsonType.getClassName());
        return value;
    }

    public static void main(String[] args) {
        String result = JsonUtils.toJSONString(new NullPointerException());
        Object result1 = JSONObject.toJSON(new NullPointerException());

        System.out.println(result);
        System.out.println(result1);
    }
}
