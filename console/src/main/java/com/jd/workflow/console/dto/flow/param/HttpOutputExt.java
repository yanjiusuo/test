package com.jd.workflow.console.dto.flow.param;

import com.jd.workflow.console.dto.requirement.AssertionStatisticsDTO;
import com.jd.workflow.flow.core.output.HttpOutput;
import lombok.Data;

import java.util.List;

@Data
public class HttpOutputExt extends HttpOutput {
    Object debugContent;

    /**
     * 断言结果
     */
    List assertionResult;
    /**
     * 断言统计
     */
    AssertionStatisticsDTO AssertionStatistics;

    /**
     * 原始入参
     */
    Object param;

    /**
     * 渲染参数
     */
    Object render;

    /**
     * 步骤错误
     */
    List stepMsg;
}
