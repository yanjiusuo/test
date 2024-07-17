package com.jd.workflow.console.entity.jacoco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/28
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoverageReport {
    /**
     * 新增代码分支覆盖率
     */
    private String totalBranches;
    /**
     * 新增代码行覆盖率
     */
    private String totalLines;
    /**
     * 报告类型
     */
    private String reportType;
    /**
     * 当前分支
     */
    private String currentBranch;
    /**
     * 基础分支
     */
    private String baseBranch;
    /**
     * 注释覆盖率
     */
    private String javaDoc;

    /**
     * coding地址
     */
    private String remoteUrl;
}
