package com.jd.workflow.console.dto.remote;

import lombok.Data;

/**
 * 行云需求信息
 */
@Data
public class DemandInfo {
    Long id;
    String dmName;
    String dmCode;
    String projectName;
    String projectCode;
    String projectId;
}
