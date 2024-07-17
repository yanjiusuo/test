package com.jd.workflow.console.dto.test.deeptest.step;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26 
 */
@Data
public class CompareRuleKV {
    /**
     * 对比类型 0:JSONPATH;
     */
    private String type;
    /**
     * 对比字段
     */
    private String path;
    /**
     * 对比规则
     */
    private String condition;
    /**
     * 期望值
     */
    private String expected;
    /**
     * 备注
     */
    private String note;
    /**
     * 提取变量的模板ID
     */
    private Long templateId;
    /**
     * 提取变量的模块ID
     */
    private Long lineId;
    /**
     * 提取变量的描述内容设置
     */
    private String varDescription;
}
