package com.jd.workflow.console.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * @Author yuanshuaiming
 * @Date 2022/12/15 8:07 下午
 * @Version 1.0
 */
@NoArgsConstructor
@Data
public class EasyDataParamDTO {

    @JsonProperty("requestId")
    private String requestId;
    @JsonProperty("appToken")
    private String appToken;
    @JsonProperty("apiGroupName")
    private String apiGroupName;
    @JsonProperty("apiName")
    private String apiName;
    @JsonProperty("pageNumber")
    private Integer pageNumber = 1;
    @JsonProperty("pageSize")
    private Integer pageSize = 10;
    @JsonProperty("params")
    private Map<String, String> params;
    @JsonProperty("stringSubs")
    private Map<String, String> stringSubs;
}
