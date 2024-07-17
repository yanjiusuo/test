package com.jd.workflow.console.service.remote.dto;

import lombok.Data;

import java.util.Map;

@Data
public class Person {
    private String id;
    private Object obj;
    Map<String,Object> map;
    private String name;
}
