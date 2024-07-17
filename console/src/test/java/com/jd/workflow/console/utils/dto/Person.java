package com.jd.workflow.console.utils.dto;

import lombok.Data;

import java.util.Set;

@Data
public class Person {
    Long sid;
    String name;
    Object obj;
    Set<String> strs;
}

