package com.jd.workflow.console.dto.ratelimiting;

import lombok.Data;

import java.util.List;

@Data
public class RateLimitingChangeStatusDTO {
    /**
     * 规则id
     */
    private List<Long> idList;

    /**
     * 状态
     */
    private Integer status;
}
