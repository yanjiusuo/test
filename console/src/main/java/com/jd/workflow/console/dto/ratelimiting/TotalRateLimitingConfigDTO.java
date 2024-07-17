package com.jd.workflow.console.dto.ratelimiting;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

@Data
public class TotalRateLimitingConfigDTO {

    //{
    //  "appName": "iam",
    //  "enableRateLimit": true,
    //  "totalRate": 400,
    //  "rejectAllEmptyAppIdRequest":false
    //  "limitList": []
    //}
    private String appName;
    private Boolean enableRateLimit;
    private Integer totalRate;
    @JsonIgnore
    private Boolean rejectAllEmptyAppIdRequest;
    private List<String> limitList;
}
