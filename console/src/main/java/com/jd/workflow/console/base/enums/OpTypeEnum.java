package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目名称：parent
 * 类 名 称：OpTypeEnum
 * 类 描 述: 操作类型
 * 创建时间：2022-11-22 10:51
 * 创 建 人：wangxiaofei8
 */
@AllArgsConstructor
public enum OpTypeEnum {

    CONSTANT("0","无变化"),

    ADD("1","新增"),

    MODIFY("2","修改"),

    DELETE("3","删除");

    @Getter
    @Setter
    private String code;

    @Getter
    @Setter
    private String description;


    public static OpTypeEnum getByCode(String code){
        for (OpTypeEnum value : OpTypeEnum.values()) {
            if(value.getCode().equals(code)){
                return value;
            }
        }
        return null;
    }
}
