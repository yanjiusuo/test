package com.jd.workflow.flow.core.processor.impl;

import com.jd.workflow.flow.core.input.MapInput;
import com.jd.workflow.flow.core.metadata.impl.CollectStepMetadata;
import com.jd.workflow.flow.core.metadata.impl.MockStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.MapOutput;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.utils.JsonValidateUtils;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class MockStepProcessor implements StepProcessor<MockStepMetadata> {
    static final Logger logger = LoggerFactory.getLogger(OutputCollectProcessor.class);

    MockStepMetadata metadata;


    @Override
    public void init(MockStepMetadata args) {
        this.metadata = args;
    }

    @Override
    public String getTypes() {
        return "mock";
    }

    @Override
    public void process(Step currentStep) {
        Map<String,Object> input = metadata.getInput();
        if(input == null) input = new HashMap<>();
        Map<String,Object> output = metadata.getOutput();
        if(output == null) output = new HashMap<>();
        currentStep.setOutput(new MapOutput(output));
        currentStep.setInput(new MapInput(input));
    }
}
