package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum NodeTypeEnum {
    DEFAULT(0,"默认"),
    SINGLE(1,"单节点"),
    MULTI(2,"多节点"),
    ;
    /**
     *
     */
    @Getter
    @Setter
    private Integer code;
    /**
     *
     */
    @Getter
    @Setter
    private String description;
}
