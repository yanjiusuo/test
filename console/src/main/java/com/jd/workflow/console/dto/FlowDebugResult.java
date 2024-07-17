package com.jd.workflow.console.dto;

import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.step.StepContext;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class FlowDebugResult {
    StepContext stepContext;
    Output output;
    public Map<String,Object> toMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("output",output);
        map.put("stepContext",stepContext.toMap());
        return map;
    }

}
