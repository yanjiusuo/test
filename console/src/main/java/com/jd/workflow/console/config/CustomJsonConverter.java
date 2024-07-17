package com.jd.workflow.console.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.jd.workflow.soap.common.util.json.CustomModule;

import java.text.SimpleDateFormat;

public class CustomJsonConverter extends ObjectMapper {
    public CustomJsonConverter(){
        super();
        //设置null转换""
        //设置日期转换yyyy-MM-dd HH:mm:ss
        setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        //对象的所有字段全部列入
        setSerializationInclusion(JsonInclude.Include.ALWAYS);
        //取消默认转换timestamps形式
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,false);
        //忽略空Bean转json的错误
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS,false);

        //忽略 在json字符串中存在，但是在java对象中不存在对应属性的情况。防止错误
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
        registerModule(new CustomModule());
    }
}