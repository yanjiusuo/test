package com.jd.workflow.console.entity.usecase;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/22
 */
@Data
@Accessors(chain = true)
public class CaseSetExeLogDetailParam extends PageParam {
    /**
     * 用例集执行记录ID
     */
    @NotNull
    @Min(value = 0,message = "属性caseSetExeLogId需大于零。")
    private Long caseSetExeLogId;
}
