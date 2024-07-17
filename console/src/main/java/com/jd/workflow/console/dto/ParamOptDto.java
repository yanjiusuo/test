package com.jd.workflow.console.dto;

import lombok.Data;

/**
 * @description:前置后置操作
 * @author: sunchao81
 * @Date: 2024-05-21
 */
@Data
public class ParamOptDto {

    /**
     * 操作类型
     */
    private String type;

    /**
     * 操作名称
     */
    private String name;

    /**
     * 关联ID
     */
    private Long referId;

    /**
     *
     */
    private Long sort;

    /**
     * 断言类型
     */
    private String paramType;

    /**
     * jsonPath参数
     */
    private Object jsonPath;

    /**
     * 断言文本
     */
    private Object paramText;

}
