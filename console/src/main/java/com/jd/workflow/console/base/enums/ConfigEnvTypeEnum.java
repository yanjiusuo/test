package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @description: 环境类型
 * @author: chenyufeng18
 * @Date: 2021/8/3
 */
@AllArgsConstructor
public enum ConfigEnvTypeEnum {
    /**
     *
     */
    APP(0,"应用"),
    /**
     *
     */
    DEMAND(1,"需求空间");

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
