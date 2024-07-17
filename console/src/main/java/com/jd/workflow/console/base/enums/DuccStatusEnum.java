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
public enum DuccStatusEnum {
    /**
     *
     */
    INVALID(0,false,"无效"),
    /**
     *
     */
    VALID(1,true,"有效");

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

        for (DuccStatusEnum c : DuccStatusEnum.values()) {
            if (code.equals(c.getCode())) {
                return c.valid;
            }
        }
        return false;
    }

    /**
     * 转化枚举
     */
    public static DuccStatusEnum getEnumByCode(String code){
        if(StringUtils.isBlank(code)){
            return null;
        }

        for (DuccStatusEnum c : DuccStatusEnum.values()) {
            if (c.code.equals(code)) {
                return c;
            }
        }

        return null;
    }

    /**
     * 转化枚举
     */
    public static DuccStatusEnum getEnumByValue(boolean valid){
        for (DuccStatusEnum c : DuccStatusEnum.values()) {
            if (c.isValid() == valid) {
                return c;
            }
        }

        return null;
    }

}
