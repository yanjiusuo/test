package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @description: MOck数据
 * @author: chenyufeng18
 * @Date: 2021/8/3
 */
@AllArgsConstructor
public enum MockFlagEnum {
    /**
     *
     */
    NOTMOCK(0,"非MOCK"),
    /**
     *
     */
    MOCK(1,"MOCK");

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
