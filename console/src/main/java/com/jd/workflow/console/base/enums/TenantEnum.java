package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum TenantEnum {
    DEFAULT(0,"默认租户"),
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
