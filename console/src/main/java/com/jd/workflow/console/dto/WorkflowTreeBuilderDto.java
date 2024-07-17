package com.jd.workflow.console.dto;

import com.jd.workflow.flow.core.definition.WorkflowInputDefinition;
import lombok.Data;

import java.util.Map;

/**
 * 构造请求树
 */
@Data
public class WorkflowTreeBuilderDto {
    Map<String,Object> definition;
    String currentStepKey;

    Long flowId;
}
