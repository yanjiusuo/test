package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2021/8/3
 */
@AllArgsConstructor
public enum ForceValidEnum {
    /**
     *
     */
    INVALID("0",false,"无效"),
    /**
     *
     */
    VALID("1",true,"有效");

    /**
     *
     */
    @Getter
    @Setter
    private String code;

    /**
     *
     */
    @Getter
    @Setter
    private boolean valid;
    /**
     *
     */
    @Getter
    @Setter
    private String description;

    // 普通方法
    public static boolean getValue(String code) {
        if(StringUtils.isBlank(code)){
            return false;
        }

        for (ForceValidEnum c : ForceValidEnum.values()) {
            if (code.equals(c.getCode())) {
                return c.valid;
            }
        }
        return false;
    }

    /**
     * 转化枚举
     */
    public static ForceValidEnum getEnumByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }

        for (ForceValidEnum c : ForceValidEnum.values()) {
            if (c.code.equals(code)) {
                return c;
            }
        }

        return null;
    }


}
