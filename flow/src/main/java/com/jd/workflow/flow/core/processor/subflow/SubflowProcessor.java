package com.jd.workflow.flow.core.processor.subflow;

import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.impl.SubflowStepMetadata;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParamMappingContext;
import com.jd.workflow.flow.utils.ParametersUtils;

import java.util.HashMap;
import java.util.Map;

public class SubflowProcessor implements StepProcessor<SubflowStepMetadata> {
    SubflowStepMetadata metadata;
    ParametersUtils utils = new ParametersUtils();
    @Override
    public void init(SubflowStepMetadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public String getTypes() {
        return "subflow";
    }

    @Override
    public void process(Step currentStep) {
        SubflowStepMetadata.Input input = metadata.getInput();
        WorkflowInput workflowInput = new WorkflowInput();
        currentStep.setInput(workflowInput);
        if(metadata.getInput().getPreProcess() != null){
            Map<String, Object> vars = utils.getMvelExecVars(currentStep.getContext());
            vars.put("input",workflowInput);
            MvelUtils.eval(metadata.getId(),"preProcess", metadata.getInput().getPreProcess(),vars,workflowInput);
        }
        if(input.getScript() != null){
            Map<String, Object> vars = utils.getMvelExecVars(currentStep.getContext());
            vars.put("input",workflowInput);
            MvelUtils.eval(metadata.getId(),"script", input.getScript(),vars,workflowInput);

        }else{
            Map<String,Object> extArgs = new HashMap<>();
            extArgs.put("input",workflowInput.attrsMap());
            ParamMappingContext paramMappingContext = new ParamMappingContext(currentStep.getContext(),extArgs);

            workflowInput.setParams(MvelUtils.getJsonInputValue(input.getParams(),paramMappingContext,workflowInput,metadata.getId()));
            workflowInput.setHeaders(MvelUtils.getJsonInputValue(input.getHeaders(),paramMappingContext,workflowInput,metadata.getId()));
            workflowInput.setBody(MvelUtils.getJsonInputValue(input.getBody(),paramMappingContext,workflowInput,metadata.getId()));
        }


        Output output = currentStep.getContext().execSubflow(currentStep.getId(), workflowInput);
        currentStep.setOutput(output);

    }
}
