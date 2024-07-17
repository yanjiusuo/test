package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.definition.WorkflowInputDefinition;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import lombok.Data;

@Data
public class InputValidateMetadata extends StepMetadata {
    WorkflowInputDefinition input;

    @Override
    public void init() {
        super.init();
        if(input != null){
            input.init();
        }
    }
}
