package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum AuthorizationKeyTypeEnum {
    INTERFACE(0),
    METHOD(1),
    RELATION(2),
    ;
    private final int type;

}
