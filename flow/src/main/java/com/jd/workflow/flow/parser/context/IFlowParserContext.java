package com.jd.workflow.flow.parser.context;

import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.metadata.impl.SubflowStepMetadata;
import lombok.Data;

/**
 * 解析上下文，用来处理
 */

public interface IFlowParserContext {
    /**
     * 根据flowId解析flow
     * @param entityId
     * @return
     */
     public WorkflowDefinition resolveSubflow(String entityId, SubflowStepMetadata metadata);

    /**
     * 解析的时候需要递归解析子流程定义，有多个子流程的时候都需要拉取出来，因此需要知道当前正在处理哪个流程
     * @param entityId
     */
    public void pushFlowId(String entityId);
     public void removeFlowId(String entityId);
     public boolean isValidate();
     public void setValidate(boolean validateStep);
     public boolean isIgnoreErrorStep();
     public void setIgnoreErrorStep(boolean ignoreErrorStep);
}
