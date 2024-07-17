package com.jd.workflow.console.dto.mock;

import lombok.Data;

import java.util.List;
/*
mock数据
 */
@Data
public class MockDocDto {
    /**
     * mock名称
     */
    String type;
    /**
     * mock描述
     */
    String desc;
    /**
     * mock函数
     */
    String function;
    List<MockExample> examples;
    /**
     * mock示例
     */
    @Data
    public static class MockExample{
        /**
         * mock示例
         */
        String example;
        /**
         * 描述
         */
        String desc;
        /**
         * 示例结果
         */
        String result;
    }
}
