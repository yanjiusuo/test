package com.jd.workflow.console.utils.dto;

import lombok.Data;

@Data
public class Child {
    private String name;
    private int age;
    Parent parent;
}
