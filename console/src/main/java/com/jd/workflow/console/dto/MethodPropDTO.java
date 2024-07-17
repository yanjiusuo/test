package com.jd.workflow.console.dto;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/10
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/10
 */
@Data
public class MethodPropDTO {
    /**
     * 属性名称
     */
    private String propName;

    /**
     * 属性类型
     */
    private String propType;

    /**
     * 属性说明
     */
    private String propDesc;
    /**
     * 出现次数
     */
    private Double count;
}
