package com.jd.workflow.console.dto.flow.param;

import lombok.Data;

@Data
public class FlowDebugLogDto {
    private Long methodId;
    private boolean success;
    /**
     * 站点：local、China、test
     */
    private String site;
    /**
     * 环境名称
     */
    private String envName;
    /**
     * 输入数据,针对http接口： {input:{headers,body,params,path},output:{headers,body}}
     * 针对jsf接口：{methodId、url、input、inputData、interfaceName、methodName、alias},output:{exception、body}
     */
    private String logContent;

    private String digest;
}
