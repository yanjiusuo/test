package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目名称：parent
 * 类 名 称：AuthLevel
 * 类 描 述：调用级别
 * 创建时间：2022-11-16 15:14
 * 创 建 人：wangxiaofei8
 */
@AllArgsConstructor
public enum AuthLevel {


    INTERFACE("0","接口"),

    METHOD("1","方法");

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String description;

    public static AuthLevel getByCode(String code){
        for (AuthLevel value : AuthLevel.values()) {
            if(value.getCode().equals(code)){
                return value;
            }
        }
        return null;
    }
}
