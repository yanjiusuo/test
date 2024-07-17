package com.jd.workflow.console.entity.usecase;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/21
 */
@Data
@Accessors(chain = true)
public class PageCaseSetExeLogDetailParam {

    /**
     * 当前页号，默认1
     */
    private Long current = 1L;

    /**
     * 页大小 最大100条，默认10
     */
    @Max(value = 100L, message = "属性pageSize不能大于100")
    private Long pageSize = 10L;

    /**
     * 用例集Id
     */
    @NotNull
    @Min(value = 0L, message = "属性caseSetExeLogId需大于零")
    private Long caseSetExeLogId;


}
