package com.jd.workflow.local.entity;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class Person {
    Long id;
    String name;
    Integer age;
    Set<String> datas;
    Map<String,Object> data;
}
