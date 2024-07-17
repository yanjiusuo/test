package com.jd.workflow.console.entity;

import lombok.Data;

import java.util.List;

@Data
public class Menu {
    private String name;
    private Integer code;
    private List<Menu> child;
}
