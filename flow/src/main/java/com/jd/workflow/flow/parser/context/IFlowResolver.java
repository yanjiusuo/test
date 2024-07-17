package com.jd.workflow.flow.parser.context;

import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.metadata.impl.SubflowStepMetadata;

/**
 * 根据di确定子流程
 */
public interface IFlowResolver {
    public WorkflowDefinition resolveSubflow(String entityId, SubflowStepMetadata metadata);
}
