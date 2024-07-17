package com.jd.workflow.console.entity.dashboard;

import lombok.Data;

@Data
public class DashBoardInfo {
    /**
     * 应用id
     */
    private Integer appId;
    private String appCode;
    private String appName;
    /**
     * jsf和http接口总数
     */
    private Long interfaceTotalCount;
}
