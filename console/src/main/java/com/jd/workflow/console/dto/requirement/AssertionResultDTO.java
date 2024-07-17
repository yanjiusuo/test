package com.jd.workflow.console.dto.requirement;

import groovy.lang.Tuple4;
import lombok.Data;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-23
 */
@Data
public class AssertionResultDTO {

    /**
     * 位置，以jsonPath格式显示
     */
    private String position;

    /**
     * 断言字段
     */
    private String key;

    /**
     * 断言的值
     */
    private String value;

    /**
     * 是否通过
     */
    private boolean result;

    /**
     *
     * @param tuple
     * @return
     */
    public static AssertionResultDTO fromTuple(Tuple4 tuple) {
        AssertionResultDTO dto = new AssertionResultDTO();
        dto.setPosition(tuple.getV1()+"");
        dto.setKey(tuple.getV2()+"");
        dto.setValue(tuple.getV3()+"");
        dto.setResult((Boolean) tuple.getV4());
        return dto;
    }
}
