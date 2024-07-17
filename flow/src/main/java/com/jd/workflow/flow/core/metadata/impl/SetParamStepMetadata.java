package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.definition.WorkflowParam;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import lombok.Data;

import java.util.List;

@Data
public class SetParamStepMetadata extends StepMetadata {
    List<WorkflowParam> params;
}
