package com.jd.workflow.flow.core.metadata;

import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;

public abstract class FallbackStepMetadata extends StepMetadata {
    public abstract TaskDefinition getTaskDef();
    public abstract CustomMvelExpression getSuccessCondition();
}
