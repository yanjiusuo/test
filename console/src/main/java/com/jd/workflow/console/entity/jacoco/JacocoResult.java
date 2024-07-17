package com.jd.workflow.console.entity.jacoco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/27
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JacocoResult {

    /**
     * 执行结果：
     * 0-执行失败  10-执行中  20-执行成功
     */
    private Integer status;

    /**
     * jacoco的结果目录
     */
    private String jacocoResultFileUrl;

    /**
     * 执行过程的日志，会返回从开始执行到执行完成的日志
     */
    private String logs;

    /**
     * 执行异常信息，包含在logs里
     */
    private String error;
}
