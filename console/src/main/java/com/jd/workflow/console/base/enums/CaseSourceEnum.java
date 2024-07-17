package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-07-03
 */
@AllArgsConstructor
public enum CaseSourceEnum {
    /**
     *
     */
    japi(1,"japi","japi在线联调"),

    /**
     *
     */
    deeptest(2,"deeptest","deeptest测试平台"),

    /**
     *
     */
    idea(3,"idea","idea插件");

    /**
     *
     */
    @Getter
    @Setter
    private Integer index;

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
    private String desc;

    /**
     *
     * @param code
     * @return
     */
    public static CaseSourceEnum getByCode(String code){
        for (CaseSourceEnum value : CaseSourceEnum.values()) {
            if(value.getCode().equals(code)){
                return value;
            }
        }
        return null;
    }
}
