package com.jd.workflow.flow.core.retry;

import lombok.Data;

@Data
public class RetryConfig {
    /**
     * 重试策略
     */
    RetryPolicy retryPolicy;
    /**
     * 策略模式
     */
    ExecuteInterval executeInterval;
}
