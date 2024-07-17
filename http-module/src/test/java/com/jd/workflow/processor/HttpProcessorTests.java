package com.jd.workflow.processor;


import com.jd.workflow.HttpBaseTestCase;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.core.metadata.impl.HttpStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.core.processor.impl.HttpStepProcessor;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpProcessorTests extends HttpBaseTestCase {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    static WorkflowInput newWorkflowInput() {
        WorkflowInput input = new WorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        input.setParams(params);
        return input;
    }

    Step newStep() {
        StepContext stepContext = new StepContext();
        stepContext.setInput(newWorkflowInput());

        Step step = new Step();
        step.setContext(stepContext);
        return step;
    }

    private HttpStepProcessor newProcessor(String path) {
        HttpStepProcessor processor = new HttpStepProcessor();
        String httpDef = getResourceContent(path);
        HttpStepMetadata httpMetadata = (HttpStepMetadata) StepProcessorRegistry.parseMetadata(JsonUtils.parse(httpDef, Map.class));
        httpMetadata.init();
        processor.init(httpMetadata);
        return processor;
    }

    @Test
    public void testValueTransform() {
        String content = getResourceContent("classpath:http/deep-list.json");
        List<JsonType> jsonTypes = JsonUtils.parseArray(content, JsonType.class);
        Object exprValue = jsonTypes.get(0).toExprValue();
        Object jsonValue = jsonTypes.get(0).toExprValue();
        Object descJson = jsonTypes.get(0).toDescJson();
        System.out.println(JsonUtils.toJSONString(exprValue));
        System.out.println(JsonUtils.toJSONString(jsonValue));
        System.out.println(JsonUtils.toJSONString(descJson));
    }

    @Test
    public void test1() {
        HttpStepProcessor processor = newProcessor("classpath:http/http1.json");

        Step step = newStep();

        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();
        assertEquals("This is the response", output.getBody());
    }

    /**
     * 所有参数均有映射
     */
    @Test
    public void testAllParamMapping() {
        //String content = getResourceContent("classpath:http/http-tasks.json");
        //WorkflowDefinition def = WorkflowParser.parse(content);
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> body = new HashMap<>();
        body.put("id",1);
        body.put("name","name");
        workflowInput.setBody(body);
        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:http/http-tasks.json");
        assertEquals("{\"path\":\"/1\",\"headers\":{\"token\":\"1\"},\"body\":{\"name\":\"name\",\"id\":1},\"params\":{\"id\":\"1\"}}",JsonUtils.toJSONString(output.getBody()));
    }
    /**
     * string-json值
     */
    @Test
    public void testStringJsonParam() {
        //String content = getResourceContent("classpath:http/http-tasks.json");
        //WorkflowDefinition def = WorkflowParser.parse(content);
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> body = new HashMap<>();
        body.put("id",1);
        body.put("name","name");
        workflowInput.setBody(body);
        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:http/http-string-json.json");
        assertEquals("{\"path\":\"/1\",\"headers\":{\"token\":\"{\\\"name\\\":\\\"name\\\",\\\"id\\\":1}\"},\"body\":{\"name\":\"{\\\"name\\\":\\\"name\\\",\\\"id\\\":1}\",\"sid\":null},\"params\":{\"id\":\"{\\\"name\\\":\\\"name\\\",\\\"id\\\":1}\"}}",JsonUtils.toJSONString(output.getBody()));
    }
    @Test
    public void testStringXmlParam() {
        //String content = getResourceContent("classpath:http/http-tasks.json");
        //WorkflowDefinition def = WorkflowParser.parse(content);
        WorkflowInput workflowInput = new WorkflowInput();
        String body = "<person><id>1</id><name>name</name></person>";

        workflowInput.setBody(body);
        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:http/http-string-xml.json");
        assertEquals("{\"headers\":{\"token\":\"<person><id>1</id><name>name</name></person>\"},\"body\":{\"name\":\"<person><id>1</id><name>name</name></person>\",\"sid\":null},\"params\":{\"id\":\"<person><id>1</id><name>name</name></person>\"}}",JsonUtils.toJSONString(output.getBody()));
    }
    @Test
    public void testFallbackStop() {
        WorkflowInput workflowInput = new WorkflowInput();
        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:http/http-fallback-stop.json");
        String body = (String) output.getBody();
        assertTrue(body.contains("This is the response"));
    }

    @Test
    public void testFallbackContinue() {
        WorkflowInput workflowInput = new WorkflowInput();
        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:http/http-fallback-continue.json");
        assertEquals("{\"a\":1}", JsonUtils.toJSONString(output.getBody()));
        assertEquals(200, output.getStatus());
    }

    @Test
    public void testFallbackSuccessCondition() {
        WorkflowInput workflowInput = new WorkflowInput();
        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:http/http-fallback-success-condition.json");
        assertEquals("{\"a\":1}", JsonUtils.toJSONString(output.getBody()));
        assertEquals(200, output.getStatus());
    }

    @Test
    public void testBody() {
        HttpStepProcessor processor = newProcessor("classpath:http/http-body.json");

        Step step = newStep();

        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();
        assertTrue(output.getBody() instanceof Map);


        assertEquals("{\"name\":\"wjf\",\"id\":1}", JsonUtils.toJSONString(output.getBody()));
    }

    @Test
    public void testForm() {
        HttpStepProcessor processor = newProcessor("classpath:http/http-form.json");
        Map<String, Object> body = new HashMap<>();

        Step step = newStep();

        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();
        assertTrue(output.getBody() instanceof Map);


        assertEquals("{\"name\":\"wjf\",\"id\":\"1\"}", JsonUtils.toJSONString(output.getBody()));
    }

    @Test
    public void testError() {
        HttpStepProcessor processor = newProcessor("classpath:http/http-error.json");

        Step step = newStep();
        try {
            processor.process(step);
            HttpOutput output = (HttpOutput) step.getOutput();
            assertEquals("This is the response", output.getBody());
        } catch (Exception e) {
            assertTrue(e instanceof StepExecException);
            HttpOutput output = (HttpOutput) step.getOutput();
            assertEquals(400, output.getStatus());

        }
    }

    @Test
    public void testScript() {
        HttpStepProcessor processor = newProcessor("classpath:http/http-script.json");

        Step step = newStep();
        WorkflowInput input = newWorkflowInput();
        Map<String, Object> body = new HashMap<>();
        body.put("id", 1);
        body.put("name", "name");
        input.setBody(body);
        step.getContext().setInput(input);
        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();
        assertEquals("{\"id\":1,\"name\":\"name\"}", JsonUtils.toJSONString(output.getBody()));

    }

    @Test
    public void testPreProcess() {
        HttpStepProcessor processor = newProcessor("classpath:http/http-pre-script.json");

        Step step = newStep();
        WorkflowInput input = newWorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("name", "name");
        input.setBody(params);
        step.getContext().setInput(input);
        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();
        assertEquals("{\"id\":1}", JsonUtils.toJSONString(output.getBody()));

    }

    @Test(expected = StepExecException.class)
    public void testPreProcessException() {
        HttpStepProcessor processor = newProcessor("classpath:http/http-pre-script-exception.json");

        Step step = newStep();
        WorkflowInput input = newWorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("name", "name");
        input.setBody(params);
        step.getContext().setInput(input);
        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();
        assertEquals("{\"id\":1}", JsonUtils.toJSONString(output.getBody()));
    }

    @Test(expected = StepExecException.class)
    public void testStringUtils() {
        HttpStepProcessor processor = newProcessor("classpath:http/http-pre-script-utils.json");

        Step step = newStep();
        WorkflowInput input = newWorkflowInput();
        Map<String, Object> body = new HashMap<>();
        body.put("id", 1);
        body.put("name", "name");
        input.setBody(body);
        step.getContext().setInput(input);
        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();
        assertEquals("{\"id\":1}", JsonUtils.toJSONString(output.getBody()));
    }

    @Test
    public void testList() {
        HttpStepProcessor processor = newProcessor("classpath:http/http-pre-script-list.json");

        Step step = newStep();
        WorkflowInput input = newWorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("name", "name");
        List list = new ArrayList();
        list.add(params);
        input.setBody(list);
        step.getContext().setInput(input);
        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();
        assertEquals("{\"id\":1}", JsonUtils.toJSONString(output.getBody()));
    }

    @Test
    public void testListParams() {
        HttpStepProcessor processor = newProcessor("classpath:http/http-params.json");

        Step step = newStep();
        WorkflowInput input = newWorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("name", new String[]{"1", "2"});

        input.setParams(params);
        step.getContext().setInput(input);
        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();
       /* String[] arr = (String[]) output.getInput().getParams().get("name");
        assertEquals("1",arr[0]);
        assertEquals("2",arr[1]);*/

    }
    @Test
    public void testCookie(){}

  /*  @Test(expected = StepExecException.class)
    public void testTimeout(){


        HttpStepProcessor processor = newProcessor("classpath:http/http-timeout.json");

        Step step = newStep();

        processor.process(step);
        HttpOutput output = (HttpOutput) step.getOutput();



        assertEquals("This is the response",output.getBody());

    }*/
}
