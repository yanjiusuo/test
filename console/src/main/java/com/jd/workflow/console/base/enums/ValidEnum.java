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
public enum ValidEnum {
    /**
     *
     */
    INVALID("0",false,"降级"),
    /**
     *
     */
    VALID("1",true,"不降级");

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
            return true;
        }

        for (ValidEnum c : ValidEnum.values()) {
            if (code.equals(c.getCode())) {
                return c.valid;
            }
        }
        return true;
    }

    /**
     * 转化枚举
     */
    public static ValidEnum getEnumByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }

        for (ValidEnum c : ValidEnum.values()) {
            if (c.code.equals(code)) {
                return c;
            }
        }

        return null;
    }

}
