package com.jd.workflow.console.dto.dashboard;

import lombok.Data;

/**
 * @Author yuanshuaiming
 * @Date 2023/7/6 4:58 下午
 * @Version 1.0
 */
@Data
public class SubCountDetail {
    String timeDay;
    Long successRequest;
    Long totalRequest;
    Integer availableRate;
}
