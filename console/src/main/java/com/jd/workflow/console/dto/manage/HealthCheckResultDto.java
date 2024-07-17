package com.jd.workflow.console.dto.manage;

import lombok.Data;

@Data
public class HealthCheckResultDto {
    /**
     * 诊断结果类型，枚举字段：
     *  param-出入参 docInfo-接口描述 inputExample-入参示例 outputExample-出差示例 mockTemplate-mock模板 debugHistory-调试历史
     */
    private String type;
    /**
     * 诊断结果，true-正常，false-异常
     */
    private boolean result;
}
