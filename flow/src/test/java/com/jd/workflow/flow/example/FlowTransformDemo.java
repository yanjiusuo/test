package com.jd.workflow.flow.example;

 import com.jd.workflow.flow.core.camel.RouteBuilder;

 import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import org.apache.camel.Exchange;
 import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
 import org.apache.camel.support.DefaultExchange;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FlowTransformDemo {
    static WorkflowDefinition newWorkflowDefinition(){
        WorkflowDefinition workflow = new WorkflowDefinition();

        workflow.init();
        return workflow;
    }
   static WorkflowInput newWorkflowInput(){
        WorkflowInput input = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        params.put("pageSize",10);
        input.setParams(params);
        return input;
    }


}
