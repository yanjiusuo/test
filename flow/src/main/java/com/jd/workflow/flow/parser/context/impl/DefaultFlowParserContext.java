package com.jd.workflow.flow.parser.context.impl;

import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.metadata.impl.SubflowStepMetadata;
import com.jd.workflow.flow.parser.context.IFlowParserContext;
import com.jd.workflow.flow.parser.context.IFlowResolver;

import java.util.ArrayList;
import java.util.List;

public class DefaultFlowParserContext implements IFlowParserContext {
    IFlowResolver flowResolver;
    /**
     * 是否校验流程
     */
    boolean validate;
    /**
     * 是否忽略错误步骤
     */
    boolean ignoreErrorStep;

    List<String> flowIds = new ArrayList<>();

    public void setFlowResolver(IFlowResolver flowResolver) {
        this.flowResolver = flowResolver;
    }

    @Override
    public WorkflowDefinition resolveSubflow(String entityId, SubflowStepMetadata metadata) {
        if(flowResolver == null){
            return metadata.getDefinition();
        }
        return flowResolver.resolveSubflow(entityId,metadata);
    }

    @Override
    public void pushFlowId(String entityId) {
        if(flowIds.contains(entityId)){
            throw new StepParseException("flow.err_found_duplicated_flow");
        }
        flowIds.add(entityId);
    }

    @Override
    public void removeFlowId(String entityId) {
        flowIds.remove(flowIds.size() - 1);
    }

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }

    @Override
    public boolean isIgnoreErrorStep() {
        return ignoreErrorStep;
    }

    @Override
    public void setIgnoreErrorStep(boolean ignoreErrorStep) {
        this.ignoreErrorStep = ignoreErrorStep;
    }
}
