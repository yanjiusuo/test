package com.jd.workflow.flow.core.processor.impl;

import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.impl.InputValidateMetadata;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Environment;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.utils.JsonValidateUtils;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParametersUtils;

import java.util.Map;

public class InputValidateProcessor implements StepProcessor<InputValidateMetadata> {
    InputValidateMetadata metadata;
    @Override
    public void init(InputValidateMetadata definition) {
        this.metadata = definition;
    }

    @Override
    public String getTypes() {
        return "reqValidate";
    }

    @Override
    public void process(Step currentStep) {
        WorkflowInput input = currentStep.getContext().getInput();
        if(metadata.getInput() != null){
            if(metadata.getInput().getHeaders() != null){
                JsonValidateUtils.validate("input","headers",metadata.getInput().getHeaders(),input.getHeaders());
            }
            if(metadata.getInput().getParams() != null){
                JsonValidateUtils.validate("input","headers",metadata.getInput().getHeaders(),input.getParams());
            }
            if(metadata.getInput().getPreProcess() != null){
                ParametersUtils utils = new ParametersUtils();
                Map<String, Object> vars = utils.getMvelExecVars(currentStep.getContext());
                vars.put("input",input);
                MvelUtils.eval(metadata.getId(),"input.preProcess", metadata.getInput().getPreProcess(),vars,input);
            }
        }


        /*if(metadata.getBody() != null){
            ObjectJsonType root = (ObjectJsonType) metadata.getBody().get(0);
            List<JsonType> children = root.getChildren();
            JsonValidateUtils.validate("input","headers",metadata.getBody(),input.getBody());
        }*/
    }
}
