package com.jd.workflow.processor;

import com.jd.workflow.WebServiceBaseTestCase;
import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.impl.WebServiceStepMetadata;
import com.jd.workflow.flow.core.metadata.impl.Ws2HttpStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.StepProcessor;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.core.processor.impl.FallbackStepProcessor;
import com.jd.workflow.flow.core.processor.impl.Http2WsStepProcessor;
import com.jd.workflow.flow.core.step.EndpointUrl;
import com.jd.workflow.flow.core.step.Environment;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.flow.xml.SoapUtils;
import com.jd.workflow.service.RpcTypedWebService;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import com.jd.workflow.soap.xml.SoapOperationToJsonTransformer;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.SoapContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Server;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j

public class WebServiceProcessorTests extends WebServiceBaseTestCase {
    /**
     * {
     * input:{
     * url:'',
     * schemaType:{
     * "name": "Envelope",
     * "namespacePrefix": "soapenv",
     * "attrs": {
     * "xmlns:soapenv": "http://schemas.xmlsoap.org/soap/envelope/",
     * "xmlns:ser": "http://service.workflow.jd.com/"
     * },
     * "type": "object",
     * "children": [{
     * "name": "Body",
     * "namespacePrefix": "soapenv",
     * "type": "object",
     * "children": [{
     * "name": "test",
     * "namespacePrefix": "ser",
     * "type": "object",
     * "children": [{}]
     * }, {
     * "name": "Header",
     * "namespacePrefix": "soapenv",
     * "attrs": {
     * "  xmlns:soapenv": "http://schemas.xmlsoap.org/soap/envelope/"
     * },
     * "type": "object",
     * "children": []
     * }]
     * }
     * }
     * <p>
     * }
     * 全类型校验
     */
    @Test
    public void testFullTypedProcessor() throws Exception {
        String typeInput = getResourceContent("classpath:json/FullTypedTestData.json");
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        HttpOutput httpOutput = (HttpOutput) execFlow("test", workflowInput);
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput));

    }
    @Test
    public void testEchoStringJson() throws Exception {
        String definition = getResourceContent("classpath:ws2http/string_json_demo.json");

        WebServiceStepMetadata stepMetadata = (WebServiceStepMetadata) StepProcessorRegistry.parseMetadata(JsonUtils.parse(definition,Map.class));
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"id\":1,\"name\":\"wjf\"}";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());

        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        StepProcessor processor = new Http2WsStepProcessor();


        processor.init(stepMetadata);

        StepContext stepContext = new StepContext();

        stepContext.setInput(workflowInput);


        Step currentStep = new Step();
        currentStep.setContext(stepContext);
        processor.process(currentStep);


        HttpOutput httpOutput =  (HttpOutput) currentStep.getOutput();
        Assert.assertEquals("{\"output\":\"{\\\"name\\\":\\\"wjf\\\",\\\"id\\\":1}\"}", JsonUtils.toJSONString(httpOutput.getBody()));
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput.getBody()));
    }

    /**
     * 测试string_json类型是否会被自动转换为对象类型
     * @throws Exception
     */
    @Test
    public void testEchoJsonAutoCast() throws Exception {

        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"id\":1,\"name\":\"wjf\"}";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());

        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));


        final HttpOutput httpOutput = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:ws2http/string_json_auto_cast.json");


        Assert.assertEquals("{\"name\":\"wjf\",\"id\":1}", JsonUtils.toJSONString(httpOutput.getBody()));
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput.getBody()));
    }
    @Test
    public void testEchoXmlAutoCast() throws Exception {

        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "<person><id>123</id><name>wjf</name></person>";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());

        workflowInput.setBody(typeInput);


        final HttpOutput httpOutput = (HttpOutput) FlowTestUtils.execFlow(workflowInput, "classpath:ws2http/string_xml_auto_cast.json");


        Assert.assertEquals("{\"person\":{\"id\":\"123\",\"name\":\"wjf\"}}", JsonUtils.toJSONString(httpOutput.getBody()));
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput.getBody()));
    }
    @Test
    public void testEchoStringXml() throws Exception {
        String definition = getResourceContent("classpath:ws2http/string_xml_demo.json");

        WebServiceStepMetadata stepMetadata = (WebServiceStepMetadata) StepProcessorRegistry.parseMetadata(JsonUtils.parse(definition,Map.class));
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "<person><id>123</id><name>wjf</name></person>";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());

        workflowInput.setBody(typeInput);
        StepProcessor processor = new Http2WsStepProcessor();


        processor.init(stepMetadata);

        StepContext stepContext = new StepContext();

        stepContext.setInput(workflowInput);


        Step currentStep = new Step();
        currentStep.setContext(stepContext);
        processor.process(currentStep);


        HttpOutput httpOutput =  (HttpOutput) currentStep.getOutput();
        Assert.assertEquals("{\"output\":\"<person><id>123</id><name>wjf</name></person>\"}", JsonUtils.toJSONString(httpOutput.getBody()));
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput.getBody()));
    }
    @Test
    public void testNoOutput() throws Exception {
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"arg0\":1}";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());
        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        HttpOutput httpOutput = (HttpOutput) execFlow("noOutput", workflowInput);
        Assert.assertEquals(null, httpOutput.getBody());
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput));
    }
    @Test
    public void testQueryRoleName() throws Exception {
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"roleName\":\"admin\"}";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());
        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        HttpOutput httpOutput = (HttpOutput) execFlow("queryRole", workflowInput);
        Assert.assertEquals("{\"return\":{\"createBy\":\"admin\",\"createDate\":\"2022-06-29\",\"id\":1,\"level\":\"1\",\"roleDesc\":\"管理员\",\"roleName\":\"admin\"}}", JsonUtils.toJSONString(httpOutput.getBody()));
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput));
    }
    @Test
    public void testSumAndMulti() throws Exception {
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"arg0\":1,\"arg1\":2}";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());
        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        HttpOutput httpOutput = (HttpOutput) execFlow("sumAndMultiply", workflowInput);
        Map<String, Object> body = (Map<String, Object>) httpOutput.getBody();

        Assert.assertEquals(3, body.get("sum"));
        Assert.assertEquals(2, body.get("multiply"));
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput));
    }

    @Test(expected = StepExecException.class)
    public void testException() throws Exception {
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"arg0\":1}";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());
        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        HttpOutput httpOutput = (HttpOutput) execFlow("noThrowException", workflowInput);
        Assert.assertEquals(null, httpOutput.getBody());
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput));

    }

    @Test
    public void testExceptionFallback() throws Exception {
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"arg0\":1}";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());
        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        TaskDefinition taskDef = new TaskDefinition();
        taskDef.setFallbackStrategy(TaskDefinition.FallbackStrategy.CONTINUE);
        TaskDefinition.Fallback fallback = new TaskDefinition.Fallback();
        fallback.setValue("23");
        fallback.setOriginal("23");

        taskDef.setFallback(fallback);
        HttpOutput httpOutput = (HttpOutput) execFlow("noThrowException", workflowInput, null, null, taskDef);
        Assert.assertEquals(23, httpOutput.getBody());
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput));

    }

    @Test
    public void testNoWrappedArg() throws Exception {
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"a\":1,\"typed\":{\"id\":1,\"name\":231}}";
        Map<String, Object> headers = new HashMap<>();

        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());
        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        HttpOutput httpOutput = (HttpOutput) execFlow("noWrappedArg", workflowInput);

        Assert.assertEquals("{\"retValue\":{\"id\":1,\"name\":\"231\"}}", JsonUtils.toJSONString(httpOutput.getBody()));
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput));

    }

    @Test
    public void testHasHeader() throws Exception {
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"typed\":{\"id\":1,\"name\":231}}";
        Map<String, Object> headers = new HashMap<>();
        headers.put("a1", 1);
        headers.put("a2", 2);
        workflowInput.setHeaders(headers);

        workflowInput.setParams(new HashMap<>());
        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        HttpOutput httpOutput = (HttpOutput) execFlow("hasHeader", workflowInput);
        Assert.assertEquals(1, httpOutput.getHeaders().get("b1"));

        Assert.assertEquals("{\"retValue\":{\"id\":1,\"name\":\"231\"}}", JsonUtils.toJSONString(httpOutput.getBody()));
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput));

    }

    public HttpOutput execFlow(String opName, WorkflowInput workflowInput) throws Exception {
        return execFlow(opName, workflowInput, null, null);
    }

    public HttpOutput execFlow(String opName, WorkflowInput workflowInput, String wsdlUrl, String localPart) throws Exception {
        return execFlow(opName, workflowInput, wsdlUrl, localPart, null);
    }

    public HttpOutput execFlow(String opName, WorkflowInput workflowInput, String wsdlUrl, String localPart, TaskDefinition taskDef) throws Exception {
        if (wsdlUrl == null) {
            wsdlUrl = this.wsdlUrl;
        }
        if (localPart == null) {
            localPart = "FullTypedWebServiceServiceSoapBinding";
        }
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(new URL(wsdlUrl));
        Definition definition = readWsdlUrl();


        Binding binding = definition.getBinding(new QName("http://service.workflow.jd.com/", localPart));

        BindingOperation bindingOperation = binding.getBindingOperation(opName, null, null);
        BuilderJsonType reqEnvelop = transformer.buildSoapMessageFromInput(binding,
                bindingOperation, SoapContext.DEFAULT);

        BuilderJsonType respEnvelop = transformer.buildSoapMessageFromOutput(binding,
                bindingOperation, SoapContext.DEFAULT);

        log.info("reqEnvelop={}", JsonUtils.toJSONString(reqEnvelop.toJsonType()));
        log.info("respEnvelop={}", JsonUtils.toJSONString(respEnvelop.toJsonType()));

        JsonType reqJsonType = reqEnvelop.toJsonType();
        JsonType respJsonType = respEnvelop.toJsonType();


        boolean isWrappedMessage = SoapUtils.isWrappedMessage(reqJsonType, opName);


        JsonType test = null;
        if (isWrappedMessage) {
            test = JsonTypeUtils.get(reqJsonType, "Body", opName);
        } else {
            test = JsonTypeUtils.get(reqJsonType, "Body");
        }
        ObjectJsonType header = (ObjectJsonType) JsonTypeUtils.get(reqJsonType, "Header");
        if (header.getChildren() != null) {
            for (JsonType child : header.getChildren()) {
                child.setValue("${workflow.input.headers." + child.getName() + "}");
            }
        }
        test.setValue("${workflow.input.body}");


        Map<String, Object> jsonSchemaType = reqJsonType.toJson();
        System.out.println(JsonUtils.toJSONString(jsonSchemaType));

        Map<String, Object> args = new HashMap<>();

        Map<String, Object> output = new HashMap<>();
        output.put("schemaType", respJsonType);

        Map<String, Object> input = new HashMap<>();

        input.put("schemaType", jsonSchemaType);
        args.put("input", input);
        args.put("opName", opName);
        args.put("endpointUrl", Collections.singletonList(WebServiceBaseTestCase.WEBSERVICE_URL));
        args.put("url", "");
        args.put("type", "http2ws");
        args.put("output", output);
        if (taskDef != null) {
            args.put("taskDef", taskDef);
        }
        log.info("step_meta_is:{}", JsonUtils.toJSONString(args));


        WebServiceStepMetadata stepMetadata = (WebServiceStepMetadata) StepProcessorRegistry.parseMetadata(args);
        StepProcessor processor = null;
        if (taskDef != null) {
            processor = new FallbackStepProcessor();
        } else {
            processor = new Http2WsStepProcessor();
        }

        processor.init(stepMetadata);

        StepContext stepContext = new StepContext();

        stepContext.setInput(workflowInput);


        Step currentStep = new Step();
        currentStep.setContext(stepContext);
        processor.process(currentStep);
        return (HttpOutput) currentStep.getOutput();

    }

    @Test
    public void testRpcStyle() throws Exception {
        String serviceUrl = "http://127.0.0.1:5001/FullTypedWebService";
        String wsdlUrl = serviceUrl + "?wsdl";
        Server server = RpcTypedWebService.run(null, serviceUrl);


        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(new URL(wsdlUrl));
        Definition definition = readWsdlUrl(wsdlUrl);


        Binding binding = definition.getBinding(new QName("http://service.workflow.jd.com/", "RpcTypedWebServiceServiceSoapBinding"));

        String opName = "rpcStyle";
        BindingOperation bindingOperation = binding.getBindingOperation(opName, null, null);
        BuilderJsonType reqEnvelop = transformer.buildSoapMessageFromInput(binding,
                bindingOperation, SoapContext.DEFAULT);

        BuilderJsonType respEnvelop = transformer.buildSoapMessageFromOutput(binding,
                bindingOperation, SoapContext.DEFAULT);


        JsonType jsonType = reqEnvelop.toJsonType();


        JsonType test = JsonTypeUtils.get(jsonType, "Body", opName);
        test.setValue("${workflow.input.body}");

        Map<String, Object> jsonSchemaType = jsonType.toJson();
        System.out.println(JsonUtils.toJSONString(jsonSchemaType));

        Map<String, Object> args = new HashMap<>();

        Map<String, Object> output = new HashMap<>();
        output.put("schemaType", respEnvelop);

        Map<String, Object> input = new HashMap<>();

        input.put("schemaType", jsonSchemaType);
        args.put("input", input);
        args.put("opName", opName);
        args.put("endpointUrl", Collections.singletonList(serviceUrl));
        args.put("url", "");
        args.put("type", "http2ws");
        args.put("output", output);

        WebServiceStepMetadata stepMetadata = (WebServiceStepMetadata) StepProcessorRegistry.parseMetadata(args);

        Http2WsStepProcessor processor = new Http2WsStepProcessor();
        processor.init(stepMetadata);

        StepContext stepContext = new StepContext();
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.setParams(new HashMap<>());
        String typeInput = "{\"arg0\":1,\"arg1\":{\"id\":1,\"name\":231}}";
        workflowInput.setBody(JsonUtils.parse(typeInput, Map.class));
        stepContext.setInput(workflowInput);

        Step currentStep = new Step();
        currentStep.setContext(stepContext);
        processor.process(currentStep);

        HttpOutput httpOutput = (HttpOutput) currentStep.getOutput();
        System.out.println("result::" + JsonUtils.toJSONString(httpOutput));
        server.stop();
    }

    private Environment newEnv() {
        Environment environment = new Environment();
        Map<String, List<EndpointUrl>> endpointUrls = new HashMap<>();
        environment.setEndpointUrls(endpointUrls);
        EndpointUrl endpointUrl = new EndpointUrl();
        endpointUrl.setUrl("http://127.0.0.1:7001");
        endpointUrls.put("online", Collections.singletonList(endpointUrl));
        return environment;
    }
    public WebServiceStepMetadata buildMetadata(String opName) throws Exception {
        Definition definition = readWsdlUrl();
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(new URL(wsdlUrl));

        Binding binding = definition.getBinding(new QName("http://service.workflow.jd.com/", "FullTypedWebServiceServiceSoapBinding"));

        BindingOperation bindingOperation = binding.getBindingOperation(opName, null, null);
        BuilderJsonType reqEnvelop = transformer.buildSoapMessageFromInput(binding,
                bindingOperation, SoapContext.DEFAULT);

        BuilderJsonType respEnvelop = transformer.buildSoapMessageFromOutput(binding,
                bindingOperation, SoapContext.DEFAULT);

        log.info("reqEnvelop={}", JsonUtils.toJSONString(reqEnvelop.toJsonType()));
        log.info("respEnvelop={}", JsonUtils.toJSONString(respEnvelop.toJsonType()));

        JsonType reqJsonType = reqEnvelop.toJsonType();
        JsonType respJsonType = respEnvelop.toJsonType();
        Map<String, Object> args = new HashMap<>();

        Map<String, Object> output = new HashMap<>();
        output.put("schemaType", respJsonType);

        Map<String, Object> input = new HashMap<>();

        input.put("schemaType", reqJsonType.toJson());
        args.put("input", input);
        args.put("opName", opName);
        args.put("endpointUrl", Collections.singletonList(WebServiceBaseTestCase.WEBSERVICE_URL));
        args.put("url", "");
        args.put("type", "http2ws");
        args.put("output", output);

        log.info("step_meta_is:{}", JsonUtils.toJSONString(args));


        WebServiceStepMetadata stepMetadata = (WebServiceStepMetadata) StepProcessorRegistry.parseMetadata(args);
        return stepMetadata;
    }
    @Test
    public void testBuildTreeNodeNoWrap() throws Exception {
        ExprTreeNode parent = new ExprTreeNode("parent","object","parent");
        WebServiceStepMetadata metadata = buildMetadata("noWrappedArg");
        metadata.buildTreeNode(parent);
        assertEquals("{\"level\":0,\"key\":\"0\",\"type\":\"object\",\"label\":\"parent\",\"expr\":\"parent\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"object\",\"label\":\"输入\",\"expr\":\"parent.input\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"object\",\"label\":\"body\",\"expr\":\"parent.input.body\",\"children\":null}]},{\"level\":1,\"key\":\"0_1\",\"type\":\"object\",\"label\":\"输出\",\"expr\":\"parent.output\",\"children\":[{\"level\":2,\"key\":\"0_1_0\",\"type\":\"object\",\"label\":\"body\",\"expr\":\"parent.output.body\",\"children\":[{\"level\":3,\"key\":\"0_1_0_0\",\"type\":\"object\",\"label\":\"retValue\",\"expr\":\"parent.output.body.retValue\",\"children\":[{\"level\":4,\"key\":\"0_1_0_0_0\",\"type\":\"long\",\"label\":\"id\",\"expr\":\"parent.output.body.retValue.id\",\"children\":null},{\"level\":4,\"key\":\"0_1_0_0_1\",\"type\":\"string\",\"label\":\"name\",\"expr\":\"parent.output.body.retValue.name\",\"children\":null}]}]}]}]}",JsonUtils.toJSONString(parent));
    }
    @Test
    public void testBuildTreeNodeWrap() throws Exception {
        ExprTreeNode parent = new ExprTreeNode("parent","object","parent");
        WebServiceStepMetadata metadata = buildMetadata("queryRole");
        metadata.buildTreeNode(parent);
        assertEquals("{\"level\":0,\"key\":\"0\",\"type\":\"object\",\"label\":\"parent\",\"expr\":\"parent\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"object\",\"label\":\"输入\",\"expr\":\"parent.input\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"object\",\"label\":\"body\",\"expr\":\"parent.input.body\",\"children\":null}]},{\"level\":1,\"key\":\"0_1\",\"type\":\"object\",\"label\":\"输出\",\"expr\":\"parent.output\",\"children\":[{\"level\":2,\"key\":\"0_1_0\",\"type\":\"object\",\"label\":\"body\",\"expr\":\"parent.output.body\",\"children\":[{\"level\":3,\"key\":\"0_1_0_0\",\"type\":\"object\",\"label\":\"return\",\"expr\":\"parent.output.body.return\",\"children\":[{\"level\":4,\"key\":\"0_1_0_0_0\",\"type\":\"string\",\"label\":\"createBy\",\"expr\":\"parent.output.body.return.createBy\",\"children\":null},{\"level\":4,\"key\":\"0_1_0_0_1\",\"type\":\"string\",\"label\":\"createDate\",\"expr\":\"parent.output.body.return.createDate\",\"children\":null},{\"level\":4,\"key\":\"0_1_0_0_2\",\"type\":\"long\",\"label\":\"id\",\"expr\":\"parent.output.body.return.id\",\"children\":null},{\"level\":4,\"key\":\"0_1_0_0_3\",\"type\":\"string\",\"label\":\"level\",\"expr\":\"parent.output.body.return.level\",\"children\":null},{\"level\":4,\"key\":\"0_1_0_0_4\",\"type\":\"string\",\"label\":\"roleDesc\",\"expr\":\"parent.output.body.return.roleDesc\",\"children\":null},{\"level\":4,\"key\":\"0_1_0_0_5\",\"type\":\"string\",\"label\":\"roleName\",\"expr\":\"parent.output.body.return.roleName\",\"children\":null}]}]}]}]}",JsonUtils.toJSONString(parent));
    }
}
