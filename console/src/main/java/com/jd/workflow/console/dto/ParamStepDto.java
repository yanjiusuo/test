package com.jd.workflow.console.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description: 执行步骤信息
 * @author: sunchao81
 * @Date: 2024-05-27
 */
@Data
@AllArgsConstructor
public class ParamStepDto {

    /**
     * 执行信息
     */
    private String msg;

    /**
     * 执行阶段名称-前置、渲染、执行调用、后置、断言
     */
    private String stageName;

    /**
     * 步骤名称: 前置-[工具名称]
     */
    private String stepName;

}
