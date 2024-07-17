package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public enum ParamScriptTypeEnum {

    /**
     *
     */
    DYNAMIC_MATERIAL(1, "动态物料"),

    /**
     *
     */
    STATIC_MATERIAL(2, "静态物料"),
    ;

    /**
     *
     */
    @Getter
    @Setter
    Integer type;

    /**
     *
     */
    @Getter
    @Setter
    String desc;
}
