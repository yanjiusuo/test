package com.jd.workflow.soap.common.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.RawSerializer;
import lombok.Data;

public class CustomModule extends SimpleModule {
    public CustomModule() {
        super("CustomModule");
        addSerializer(RawString.class, new RawSerializer(RawString.class));

    }

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new CustomModule()); // 注册自定义的 Module

        // 配置其他 Jackson 的设置
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        Person person = new Person();
        RawString plainString = new RawString();
        plainString.setStr("Set[\"1\"]");
        person.setPlainString(plainString);
        person.setId(12L);
        System.out.println(objectMapper.writeValueAsString(person));

    }
    @Data
    static class Person {
        Long id = 1L;
        RawString plainString;
    }
}
