package com.jd.workflow.processor;

import com.jd.workflow.HttpBaseTestCase;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class TransformProcessorTests extends HttpBaseTestCase {
    Step newStep(){
        StepContext stepContext = new StepContext();
        stepContext.setInput(newWorkflowInput());

        Step step = new Step();
        step.setContext(stepContext);
        return step;
    }
    static WorkflowInput newWorkflowInput(){
        WorkflowInput input = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        params.put("pageSize",10);
        input.setParams(params);
        return input;
    }

     @Test
     public void testScriptTransform(){
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> body = new HashMap<>();
        body.put("name","name");
        workflowInput.setBody(body);
        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:transform/transform.json");
        System.out.println(JsonUtils.toJSONString(output));
        assertEquals("{\"token\":1}",JsonUtils.toJSONString(output.getHeaders()));
        assertEquals("{\"id\":1,\"name\":\"name\",\"value\":\"1-wjf\"}",JsonUtils.toJSONString(output.getBody()));
    }
     @Test public void testConfigTransform(){
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> body = new HashMap<>();
        body.put("name","name");
        workflowInput.setBody(body);
        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:transform/transform-config.json");
        System.out.println(JsonUtils.toJSONString(output));
        assertEquals("{\"token\":1}",JsonUtils.toJSONString(output.getHeaders()));
        assertEquals("{\"name\":\"name\",\"id\":1,\"value\":\"1-wjf\"}",JsonUtils.toJSONString(output.getBody()));
    }
}
