package com.jd.workflow.flow.core.processor.impl;

import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.definition.WorkflowParam;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.exception.StepScriptEvalException;
import com.jd.workflow.flow.core.input.Input;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
import com.jd.workflow.flow.core.metadata.impl.SetParamStepMetadata;
import com.jd.workflow.flow.core.output.BaseOutput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class SetParamProcessor implements StepProcessor<SetParamStepMetadata> {
    SetParamStepMetadata setParamStepMetadata = new SetParamStepMetadata();
    @Override
    public void init(SetParamStepMetadata metadata) {
        String type = metadata.getType();
        this.setParamStepMetadata = metadata;
    }

    @Override
    public String getTypes() {
        return "setParam";
    }

    @Override
    public void process(Step currentStep) {
        if(setParamStepMetadata.getParams() == null) return;
        Map<String,String> params = new HashMap<>();
        for (WorkflowParam param : setParamStepMetadata.getParams()) {
            params.put(param.getName(),param.getValue());
        }
        currentStep.getContext().setParams(params);
    }
}
