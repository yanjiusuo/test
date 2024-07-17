package com.jd.workflow.console.dto.ratelimiting;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RateLimitingQueryDTO {
    /**
     * 查询时此字段必填
     */
    private String  appProvider;

    private String interfaceName;

    private String appConsumer;

    private Integer status;

    private Integer ruleType;

    private String principal;

    private String tenantId;//租户id-up_jd服务总线、up_jd_api藏经阁

    private Integer current = 1;

    private Integer size = 10;

    private String erp;
}
