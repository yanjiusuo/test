package com.jd.workflow.flow.core.processor.impl;

import com.jd.workflow.flow.core.metadata.impl.CollectStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Environment;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.utils.JsonValidateUtils;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParamMappingContext;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 收集出参
 */
public class OutputCollectProcessor implements StepProcessor<CollectStepMetadata> {
    static final Logger logger = LoggerFactory.getLogger(OutputCollectProcessor.class);

    CollectStepMetadata metadata;


    @Override
    public void init(CollectStepMetadata args) {
        this.metadata = args;
    }

    @Override
    public String getTypes() {
        return "collect,transform";
    }

    @Override
    public void process(Step currentStep) {

        HttpOutput output = new HttpOutput();

        if (metadata.getOutput() == null) {
            output.setStatus(HttpStatus.SC_OK);
            output.setBody(null);
        } else {
            ParametersUtils utils = new ParametersUtils();
            output.setStatus(HttpStatus.SC_OK);
            if (metadata.getOutput().getScript() != null) {
                Map<String, Object> vars = utils.getMvelExecVars(currentStep.getContext());
                vars.put("output",output);
                MvelUtils.eval(metadata.getId(), "script", metadata.getOutput().getScript(), vars,output);
            } else {

                ParamMappingContext paramMappingContext = new ParamMappingContext(currentStep.getContext());
                Map<String, Object> map = MvelUtils.getJsonInputValue(metadata.getOutput().getHeaders(), paramMappingContext,output,"headers");
                JsonType httpBody = JsonValidateUtils.getHttpBody(metadata.getOutput().getBody());
                Object body = MvelUtils.getJsonInputValue(httpBody, paramMappingContext,output,"body");
                output.setHeaders(map);
                output.setBody(body);
            }

        }

        currentStep.setOutput(output);
        currentStep.getContext().setOutput(output);
    }
}