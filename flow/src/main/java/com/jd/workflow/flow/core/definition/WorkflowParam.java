package com.jd.workflow.flow.core.definition;

import lombok.Data;

/**
 * 流程管理里用到的公共参数，有参数名、参数值2个属性
 */
@Data
public class WorkflowParam {
    Long entityId;
    String name;
    String value;
}
