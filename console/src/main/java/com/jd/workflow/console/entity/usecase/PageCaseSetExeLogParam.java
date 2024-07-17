package com.jd.workflow.console.entity.usecase;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

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
public class PageCaseSetExeLogParam extends PageParam {

    /**
     * 用例集Id
     */
    @NotNull
    @Min(value = 0L, message = "属性caseSetId需大于零")
    private Long caseSetId;

    /**
     * 状态 1-用例待执行 2-用例执行中 3-覆盖率计算中 4-成功 5-失败
     */
    private Integer status;
    /**
     * 状态 1-个人视角 2-全部视角
     */
    private Integer queryType = 1;



}
