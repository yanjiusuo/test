package com.jd.workflow.console.base.enums;

import lombok.Getter;
import lombok.Setter;

/**
 * 环境的枚举
 * @date: 2022/5/30 14:34
 * @author wubaizhao1
 */
public enum EnvTypeEnum {
    /**
     * 测试环境
     */
    TEST,
    /**
     * 预发环境
     */
    PRE,
    LOCAL,
    /**
     * 生产环境
     */
    RELEASE,
    ;


}
