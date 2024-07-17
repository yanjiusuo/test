package com.jd.workflow.console.entity.usecase;

import lombok.Data;

import javax.validation.constraints.Max;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/22
 */
@Data
public class PageParam {

    /**
     * 当前页号，默认1
     */
    private Long current = 1L;

    /**
     * 页大小 最大100条，默认10
     */
    @Max(value = 100L, message = "属性pageSize不能大于100")
    private Long pageSize = 10L;
}
