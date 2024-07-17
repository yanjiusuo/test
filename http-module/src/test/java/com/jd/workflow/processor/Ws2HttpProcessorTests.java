package com.jd.workflow.processor;

import com.jd.workflow.HttpBaseTestCase;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.BeanStepDefinition;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.impl.Ws2HttpStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.WebServiceOutput;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.core.processor.impl.Ws2HttpStepProcessor;
import com.jd.workflow.flow.core.step.EndpointUrl;
import com.jd.workflow.flow.core.step.Environment;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.xml.SoapOperationToJsonTransformer;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import com.jd.workflow.soap.utils.WsdlUtils;
import com.jd.workflow.soap.wsdl.HttpDefinition;
import com.jd.workflow.soap.wsdl.HttpWsdlGenerator;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.Test;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 *
 */
public class Ws2HttpProcessorTests extends HttpBaseTestCase {


    public Definition httpToWsdlUrl() throws WSDLException {
        String content = getResourceContent("classpath:ws2http/http-def.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);

        return httpToWsdlUrl(definition);
    }

    public Definition httpToWsdlUrl(HttpDefinition definition) throws WSDLException {
        Definition wsdl = HttpWsdlGenerator.generateWsdlDefinition(definition);
        return wsdl;
    }

    /**
     * 整体流程：构造Ws2HttpStepMetadata对象->构造Ws2HttpStepMetadata对象 -> 构造camel xml文件
     *
     * @throws Exception
     */
    @Test
    public void testWs2WorkflowDefinition() throws Exception {

        Definition definition = httpToWsdlUrl();


        Binding binding = null;
        BindingOperation bindingOperation = null;
        for (Object o : definition.getAllBindings().entrySet()) {
            Map.Entry<QName, Binding> entry = (Map.Entry<QName, Binding>) o;
            binding = entry.getValue();
        }
        for (Object o : binding.getBindingOperations()) {
            bindingOperation = (BindingOperation) o;
        }


        SoapMessageBuilder messageBuilder = new SoapMessageBuilder(definition);
        String outputXml = messageBuilder.buildSoapMessageFromOutput(binding, bindingOperation, SoapContext.DEFAULT);

        logger.info("output_is:outputXml={}", outputXml);


        String result = WsdlUtils.wsdlToString(definition);


        logger.info("result_is:result={}", result);

        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(definition);


        JsonType reqEnvelop = transformer.buildSoapMessageFromInput(binding,
                bindingOperation, SoapContext.DEFAULT).toJsonType();

        JsonType respEnvelop = transformer.buildSoapMessageFromOutput(binding,
                bindingOperation, SoapContext.DEFAULT).toJsonType();






        /*JsonType test = JsonTypeUtils.get(jsonType,"Body","test");
        test.setValue("${workflow.input.body}");*/

        Map<String, Object> jsonSchemaType = reqEnvelop.toJson();

        Map<String, Object> args = new HashMap<>();

        Map<String, Object> output = new HashMap<>();
        output.put("schemaType", respEnvelop);

        Map<String, Object> input = new HashMap<>();

        input.put("schemaType", jsonSchemaType);
        args.put("input", input);
        args.put("id", "ws2http");
        args.put("endpointUrl", Collections.singletonList(HttpBaseTestCase.SERVICE_URL));
        args.put("url", "/json");
        args.put("httpMethod", "post");
        args.put("reqType", "json");
        args.put("type", "ws2http");
        args.put("output", output);

        Ws2HttpStepMetadata stepMetadata = (Ws2HttpStepMetadata) StepProcessorRegistry.parseMetadata(args);


        BeanStepDefinition beanDef = new BeanStepDefinition();
        beanDef.setMetadata(stepMetadata);

        WorkflowDefinition workflowDefinition = new WorkflowDefinition();
        workflowDefinition.setTasks(Collections.singletonList(beanDef));
        // 生成camel 文件
        String ret = RouteBuilder.buildRoute(workflowDefinition);
        logger.info("camel_xml={}", ret);

    }

    public HttpOutput call(HttpDefinition httpDefinition, WorkflowInput workflowInput) throws Exception {
        return call(httpDefinition, workflowInput,false);
    }
    public HttpOutput call(HttpDefinition httpDefinition, WorkflowInput workflowInput,boolean isXml) throws Exception {
        return call(httpDefinition, workflowInput,isXml,"json");
    }
    public HttpOutput call(HttpDefinition httpDefinition, WorkflowInput workflowInput,boolean isXml,String reqType) throws Exception {
        Definition definition = httpToWsdlUrl(httpDefinition);


        Binding binding = null;
        BindingOperation bindingOperation = null;
        for (Object o : definition.getAllBindings().entrySet()) {
            Map.Entry<QName, Binding> entry = (Map.Entry<QName, Binding>) o;
            binding = entry.getValue();
        }
        for (Object o : binding.getBindingOperations()) {
            bindingOperation = (BindingOperation) o;
        }


        SoapMessageBuilder messageBuilder = new SoapMessageBuilder(definition);
        String outputXml = messageBuilder.buildSoapMessageFromOutput(binding, bindingOperation, SoapContext.DEFAULT);

        logger.info("output_is:outputXml={}", outputXml);


        String result = WsdlUtils.wsdlToString(definition);


        logger.info("result_is:result={}", result);

        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(definition);


        JsonType reqEnvelop = transformer.buildSoapMessageFromInput(binding,
                bindingOperation, SoapContext.DEFAULT).toJsonType();

        JsonType respEnvelop = transformer.buildSoapMessageFromOutput(binding,
                bindingOperation, SoapContext.DEFAULT).toJsonType();

        logger.info("reqEnvelop={},respEnvelop={}",JsonUtils.toJSONString(reqEnvelop),
                JsonUtils.toJSONString(respEnvelop));

        HttpWsdlGenerator.setReqRawName(reqEnvelop,httpDefinition);
        HttpWsdlGenerator.setRespRawName(respEnvelop,httpDefinition);



        /*JsonType test = JsonTypeUtils.get(jsonType,"Body","test");
        test.setValue("${workflow.input.body}");*/

        Map<String, Object> jsonSchemaType = reqEnvelop.toJson();
        System.out.println(JsonUtils.toJSONString(jsonSchemaType));

        Map<String, Object> args = new HashMap<>();

        Map<String, Object> output = new HashMap<>();
        output.put("schemaType", respEnvelop);

        Map<String, Object> input = new HashMap<>();

        input.put("schemaType", jsonSchemaType);
        args.put("input", input);
        args.put("endpointUrl", Collections.singletonList(httpDefinition.getUrl()));
        args.put("url", "");

        args.put("httpMethod", "post");
        args.put("reqType", reqType);
        args.put("type", "ws2http");
        args.put("output", output);


        Ws2HttpStepMetadata stepMetadata = (Ws2HttpStepMetadata) StepProcessorRegistry.parseMetadata(args);

        Ws2HttpStepProcessor processor = new Ws2HttpStepProcessor();
        processor.init(stepMetadata);

        StepContext stepContext = new StepContext();

        if(!isXml){
            JsonType httpBodyJsonType = JsonTypeUtils.get(reqEnvelop, "Body", "GetPerson", "body", "root");
            JsonType httpParamJsonType = JsonTypeUtils.get(reqEnvelop, "Body", "GetPerson", "params");
            JsonType httpHeaderJsonType = JsonTypeUtils.get(reqEnvelop, "Body", "GetPerson", "headers");
            JsonType pathParamsJsonType = JsonTypeUtils.get(reqEnvelop, "Body", "GetPerson", "pathParams");

            if (httpBodyJsonType != null) {
                httpBodyJsonType.setValue(workflowInput.getBody());
            }
            if (httpParamJsonType != null) {
                httpParamJsonType.setValue(workflowInput.getParams());
            }
            if (httpHeaderJsonType != null) {
                httpHeaderJsonType.setValue(workflowInput.getHeaders());
            }
            if(pathParamsJsonType != null){
                pathParamsJsonType.setValue(workflowInput.attr("pathParams"));
            }


            Object envelop = reqEnvelop.toExprValue();

            XNode xNode = reqEnvelop.transformToXml(envelop).get(0);
            String xml = xNode.toXml();
            logger.info("the input_xml={}", xml);
            workflowInput.setBody(xml);
        }


        stepContext.setInput(workflowInput);

        Step currentStep = new Step();
        currentStep.setContext(stepContext);
        processor.process(currentStep);

        HttpOutput httpOutput = (HttpOutput) currentStep.getOutput();
        httpOutput.attr("context",stepContext);
        return httpOutput;
    }

    @Test
    public void testNoInput() throws Exception {
        String content = getResourceContent("classpath:ws2http/http-noinput-def.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);
        WorkflowInput workflowInput = new WorkflowInput();


        HttpOutput httpOutput = call(definition, workflowInput);
        String xml = (String) httpOutput.getBody();
        logger.info("the output xml is:xml={}", xml);
        System.out.println(JsonUtils.toJSONString(httpOutput));
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse>\n" +
                "         <return>\n" +
                "            <body>\n" +
                "               <root>This is the response</root>\n" +
                "            </body>\n" +
                "         </return>\n" +
                "      </wjf:GetPersonResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>", httpOutput.getBody());
    }

    @Test
    public void testNoBody() throws Exception {
        String content = getResourceContent("classpath:ws2http/http-nobody-def.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);
        WorkflowInput workflowInput = new WorkflowInput();
        HttpOutput httpOutput = call(definition, workflowInput);
        String xml = (String) httpOutput.getBody();
        logger.info("the output xml is:xml={}", xml);
        System.out.println(JsonUtils.toJSONString(httpOutput));
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse/>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>", httpOutput.getBody());
    }
    @Test
    public void testError() throws Exception {
        String content = getResourceContent("classpath:ws2http/http-error.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.addHeader("token", 123);
        HttpOutput httpOutput = call(definition, workflowInput);
        String xml = (String) httpOutput.getBody();
        logger.info("the output xml is:xml={}", xml);
        System.out.println(JsonUtils.toJSONString(httpOutput));
        String body = (String) httpOutput.getBody();
        assertTrue(body.startsWith("<"));
        assertTrue(body.contains("This is the response"));
    }
    @Test
    public void testFormRef() throws Exception {
        String content = getResourceContent("classpath:ws2http/http-form-def.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);
        WorkflowInput workflowInput = new WorkflowInput();
        String input = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPerson>\n" +
                "         <body>\n" +
                "            <!--Optional:-->\n" +
                "            <root>\n" +
                "               <!--Optional:-->\n" +
                "               <name>wjf</name>\n" +
                "               <!--Optional:-->\n" +
                "               <sid>123</sid>\n" +
                "            </root>\n" +
                "         </body>\n" +
                "      </wjf:GetPerson>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        workflowInput.setBody(input);
        HttpOutput httpOutput = call(definition, workflowInput,true,"form");
        String xml = (String) httpOutput.getBody();
        logger.info("the output xml is:xml={}", xml);
        System.out.println(JsonUtils.toJSONString(httpOutput));
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse>\n" +
                "         <return>\n" +
                "            <body>\n" +
                "               <root>\n" +
                "                  <name>wjf</name>\n" +
                "                  <sid>123</sid>\n" +
                "               </root>\n" +
                "            </body>\n" +
                "         </return>\n" +
                "      </wjf:GetPersonResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>", httpOutput.getBody());
    }
    @Test
    public void testOnlyHeaders() throws Exception {
        String content = getResourceContent("classpath:ws2http/http-onlyheaders-def.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.addHeader("token", 123);
        HttpOutput httpOutput = call(definition, workflowInput);
        String xml = (String) httpOutput.getBody();
        logger.info("the output xml is:xml={}", xml);
        System.out.println(JsonUtils.toJSONString(httpOutput));
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse>\n" +
                "         <return>\n" +
                "            <headers>\n" +
                "               <token>123</token>\n" +
                "            </headers>\n" +
                "         </return>\n" +
                "      </wjf:GetPersonResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>", httpOutput.getBody());
    }

    @Test
    public void testWebServiceProcessor() throws Exception {


        String content = getResourceContent("classpath:ws2http/http-def.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.addParam("id", "213");
        workflowInput.addHeader("token", "123");


        Map<String, Object> body = new HashMap<>();
        body.put("sid", 1);
        body.put("name", "wjf");
        workflowInput.setBody(body);
        HttpOutput httpOutput = call(definition, workflowInput);


        System.out.println(JsonUtils.toJSONString(httpOutput));
    }
    WorkflowInput newTestWorkflowInput(){
        WorkflowInput workflowInput = new WorkflowInput();
        workflowInput.addParam("XId", "213");
        workflowInput.addHeader("XToken", "123");

        Map<String, Object> body = new HashMap<>();
        body.put("XTokenX", 1);
        body.put("XTokenD", "wjf");
        workflowInput.setBody(body);
        Map<String,Object> pathParam = new HashMap<>();
        pathParam.put("XToken","test");
        pathParam.put("XIdToken",123);
        workflowInput.attr("pathParams",pathParam);
        return workflowInput;
    }
    @Test
    public void testCaseInsensitiveAllParam() throws Exception {
        String content = getResourceContent("classpath:ws2http/case-insensitive/http-all-param.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);

        HttpOutput httpOutput = call(definition, newTestWorkflowInput());

        StepContext stepContext = (StepContext) httpOutput.attr("context");

        String respBody = (String) httpOutput.getBody();

        Map<String, Object> headers = httpOutput.getResponse().getHeaders();
        Object httpBody = httpOutput.getResponse().getBody();

        assertEquals("123",headers.get("X-token"));
        assertEquals("{\"path\":\"/test/123\",\"headers\":{\"x-token\":\"123\"},\"body\":{\"X-TOKEN_D\":\"wjf\",\"x-token_x\":1},\"params\":{\"x_id\":\"213\"}}",JsonUtils.toJSONString(httpBody));
        System.out.println("---------------------------------------------------");
        String responseBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse>\n" +
                "         <return>\n" +
                "            <body>\n" +
                "               <root>\n" +
                "                  <body>\n" +
                "                     <XTokenD>wjf</XTokenD>\n" +
                "                     <XTokenX>1</XTokenX>\n" +
                "                  </body>\n" +
                "                  <headers>\n" +
                "                     <XToken>123</XToken>\n" +
                "                  </headers>\n" +
                "                  <params>\n" +
                "                     <XId>213</XId>\n" +
                "                  </params>\n" +
                "                  <path>/test/123</path>\n" +
                "               </root>\n" +
                "            </body>\n" +
                "            <headers>\n" +
                "               <XToken>123</XToken>\n" +
                "            </headers>\n" +
                "         </return>\n" +
                "      </wjf:GetPersonResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        assertEquals(responseBody,respBody);
        logger.info("responseBody={}",respBody);
        System.out.println(JsonUtils.toJSONString(httpOutput));
    }
    @Test
    public void testCaseInsensitiveOnlyBody() throws Exception {
        String content = getResourceContent("classpath:ws2http/case-insensitive/http-only-body.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);

        HttpOutput httpOutput = call(definition, newTestWorkflowInput());

        StepContext stepContext = (StepContext) httpOutput.attr("context");

        String respBody = (String) httpOutput.getBody();

        Map<String, Object> headers = httpOutput.getResponse().getHeaders();
        Object httpBody = httpOutput.getResponse().getBody();


        assertEquals("{\"headers\":{},\"body\":{\"X-TOKEN_D\":\"wjf\",\"x-token_x\":1},\"params\":null}",JsonUtils.toJSONString(httpBody));
        String responseBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse>\n" +
                "         <return>\n" +
                "            <body>\n" +
                "               <root>\n" +
                "                  <body>\n" +
                "                     <XTokenD>wjf</XTokenD>\n" +
                "                     <XTokenX>1</XTokenX>\n" +
                "                  </body>\n" +
                "                  <headers/>\n" +
                "               </root>\n" +
                "            </body>\n" +
                "         </return>\n" +
                "      </wjf:GetPersonResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        assertEquals(responseBody,respBody);
        logger.info("responseBody={}",respBody);
        System.out.println(JsonUtils.toJSONString(httpOutput));
    }
    @Test
    public void testCaseInsensitiveOnlyHeaders() throws Exception {
        String content = getResourceContent("classpath:ws2http/case-insensitive/http-only-headers.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);

        HttpOutput httpOutput = call(definition, newTestWorkflowInput());

        StepContext stepContext = (StepContext) httpOutput.attr("context");

        String respBody = (String) httpOutput.getBody();

        Map<String, Object> headers = httpOutput.getResponse().getHeaders();
        Object httpBody = httpOutput.getResponse().getBody();

        assertEquals("123",headers.get("X-token"));
        assertEquals("{\"headers\":{\"x-token\":\"123\"},\"body\":null,\"params\":null}",JsonUtils.toJSONString(httpBody));
        System.out.println("---------------------------------------------------");
        String responseBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse>\n" +
                "         <return>\n" +
                "            <headers>\n" +
                "               <XToken>123</XToken>\n" +
                "            </headers>\n" +
                "         </return>\n" +
                "      </wjf:GetPersonResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        assertEquals(responseBody,respBody);
        logger.info("responseBody={}",respBody);
        System.out.println(JsonUtils.toJSONString(httpOutput));
    }
    @Test
    public void testCaseInsensitiveOnlyParams() throws Exception {
        String content = getResourceContent("classpath:ws2http/case-insensitive/http-only-params.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);

        HttpOutput httpOutput = call(definition, newTestWorkflowInput());

        StepContext stepContext = (StepContext) httpOutput.attr("context");

        String respBody = (String) httpOutput.getBody();

        Map<String, Object> headers = httpOutput.getResponse().getHeaders();
        Object httpBody = httpOutput.getResponse().getBody();


        assertEquals("{\"headers\":{},\"body\":null,\"params\":{\"x_id\":\"213\"}}",JsonUtils.toJSONString(httpBody));
        System.out.println("---------------------------------------------------");
        String responseBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse>\n" +
                "         <return>\n" +
                "            <body>\n" +
                "               <root>\n" +
                "                  <params>\n" +
                "                     <XId>213</XId>\n" +
                "                  </params>\n" +
                "               </root>\n" +
                "            </body>\n" +
                "         </return>\n" +
                "      </wjf:GetPersonResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        assertEquals(responseBody,respBody);
        logger.info("responseBody={}",respBody);
        System.out.println(JsonUtils.toJSONString(httpOutput));
    }

    /**
     * 有数组类型参数，但是数组类型参数每一项不一致
     * @throws Exception
     */
    @Test(expected = StdException.class)
    public void testCaseInsensitiveMixArray() throws Exception {
        String content = getResourceContent("classpath:ws2http/case-insensitive/http-array-mix-type.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);

        WorkflowInput workflowInput = new WorkflowInput();
        String input = "[{\"X-Token_D\":\"213\"},[{\"x-token_x\":{\"X-TOKEN_D\":123321}},{\"x-token_x\":{\"X-TOKEN_D\":123321}}],[[[\"1\"],[\"2\"]],[[\"1\"],[\"2\"]]]]";
        workflowInput.setBody(JsonUtils.parse(input,List.class));

        HttpOutput httpOutput = call(definition, workflowInput);




        StepContext stepContext = (StepContext) httpOutput.attr("context");

        String respBody = (String) httpOutput.getBody();

        Map<String, Object> headers = httpOutput.getResponse().getHeaders();
        Object httpBody = httpOutput.getResponse().getBody();


        assertEquals("{\"headers\":{},\"body\":[{\"X-Token_D\":\"213\"},[{\"x-token_x\":{\"X-TOKEN_D\":123321}},{\"x-token_x\":{\"X-TOKEN_D\":123321}}],[[[\"1\"],[\"2\"]],[[\"1\"],[\"2\"]]]],\"params\":null",JsonUtils.toJSONString(httpBody));
        System.out.println("---------------------------------------------------");
        System.out.println(respBody);
        System.out.println(JsonUtils.toJSONString(httpOutput));
    }
    Object transformParam(Object o){
        if(o == null) return null;
        if(o instanceof Map){
            Map map = (Map) o;
            Map ret = new HashMap();
            for (Object o1 : map.entrySet()) {
                Map.Entry<String,Object> entry = (Map.Entry<String, Object>) o1;
                ret.put(StringHelper.camelParamName(entry.getKey(),true),transformParam(entry.getValue()));
            }
            return ret;
        }else if(o instanceof List){
            List list = (List) o;
            List ret = new ArrayList();
            for (Object o1 : list) {
                ret.add(transformParam(o1));
            }
            return ret;
        }
        return o;
    }
    @Test
    public void testCaseInsensitivePlainArray() throws Exception {
        String content = getResourceContent("classpath:ws2http/case-insensitive/http-array-plain-type.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);

        WorkflowInput workflowInput = new WorkflowInput();
        String input = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPerson>\n" +
                "         <body>\n" +
                "            <!--Zero or more repetitions:-->\n" +
                "            <root>\n" +
                "               <!--Optional:-->\n" +
                "               <XTokenD>321321</XTokenD>\n" +
                "               <!--Zero or more repetitions:-->\n" +
                "               <XTokenXx>\n" +
                "                  <!--Zero or more repetitions:-->\n" +
                "                  <item>321</item>\n" +
                "                  <item>213</item>\n" +
                "               </XTokenXx>\n" +
                "               <XTokenXx>\n" +
                "                  <!--Zero or more repetitions:-->\n" +
                "                  <item>321</item>\n" +
                "                  <item>213</item>\n" +
                "               </XTokenXx>\n" +
                "           </root>\n" +
                "         </body>\n" +
                "      </wjf:GetPerson>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        workflowInput.setBody(input);

        HttpOutput httpOutput = call(definition, workflowInput,true);




        StepContext stepContext = (StepContext) httpOutput.attr("context");

        String respBody = (String) httpOutput.getBody();

        Map<String, Object> headers = httpOutput.getResponse().getHeaders();
        Object httpBody = httpOutput.getResponse().getBody();


        assertEquals("{\"headers\":{},\"body\":[{\"X-TOKEN_D\":\"321321\",\"X-TOKEN_XX\":[[\"321\",\"213\"],[\"321\",\"213\"]]}],\"params\":null}",JsonUtils.toJSONString(httpBody));
        System.out.println("---------------------------------------------------");
        String responseBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse>\n" +
                "         <return>\n" +
                "            <body>\n" +
                "               <root>\n" +
                "                  <body>\n" +
                "                     <XTokenD>321321</XTokenD>\n" +
                "                     <XTokenXx>\n" +
                "                        <item>321</item>\n" +
                "                        <item>213</item>\n" +
                "                     </XTokenXx>\n" +
                "                     <XTokenXx>\n" +
                "                        <item>321</item>\n" +
                "                        <item>213</item>\n" +
                "                     </XTokenXx>\n" +
                "                  </body>\n" +
                "                  <headers/>\n" +
                "               </root>\n" +
                "            </body>\n" +
                "         </return>\n" +
                "      </wjf:GetPersonResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        assertEquals(responseBody,respBody);
        logger.info("responseBody={}",respBody);
        System.out.println(JsonUtils.toJSONString(httpOutput));
    }


    @Test
    public void testCaseInsensitiveREqRespHasArray() throws Exception {
        String content = getResourceContent("classpath:ws2http/case-insensitive/http-array-all-array.json");
        HttpDefinition definition = JsonUtils.parse(content, HttpDefinition.class);

        WorkflowInput workflowInput = new WorkflowInput();
        String input = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPerson>\n" +
                "         <body>\n" +
                "            <!--Zero or more repetitions:-->\n" +
                "            <root>\n" +
                "               <!--Zero or more repetitions:-->\n" +
                "               <friends>\n" +
                "                  <!--Zero or more repetitions:-->\n" +
                "                  <item>123</item>\n" +
                "                  <item>456</item>\n" +
                "               </friends>\n" +
                "               <friends>\n" +
                "                  <!--Zero or more repetitions:-->\n" +
                "                  <item>123</item>\n" +
                "                  <item>456</item>\n" +
                "               </friends>\n" +
                "               <!--Optional:-->\n" +
                "               <id>444</id>\n" +
                "            </root>\n" +
                "             <!--Zero or more repetitions:-->\n" +
                "            <root>\n" +
                "               <!--Zero or more repetitions:-->\n" +
                "               <friends>\n" +
                "                  <!--Zero or more repetitions:-->\n" +
                "                  <item>123</item>\n" +
                "                  <item>456</item>\n" +
                "               </friends>\n" +
                "               <friends>\n" +
                "                  <!--Zero or more repetitions:-->\n" +
                "                  <item>123</item>\n" +
                "                  <item>456</item>\n" +
                "               </friends>\n" +
                "               <!--Optional:-->\n" +
                "               <id>444</id>\n" +
                "            </root>\n" +
                "         </body>\n" +
                "      </wjf:GetPerson>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";

        workflowInput.setBody(input);

        HttpOutput httpOutput = call(definition, workflowInput,true);




        StepContext stepContext = (StepContext) httpOutput.attr("context");

        String respBody = (String) httpOutput.getBody();

        Map<String, Object> headers = httpOutput.getResponse().getHeaders();
        Object httpBody = httpOutput.getResponse().getBody();


        assertEquals("[{\"friends\":[[\"123\",\"456\"],[\"123\",\"456\"]],\"id\":\"444\"},{\"friends\":[[\"123\",\"456\"],[\"123\",\"456\"]],\"id\":\"444\"}]",JsonUtils.toJSONString(httpBody));
        System.out.println("---------------------------------------------------");
        String responseBody = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                  xmlns:wjf=\"http://wjf.com/\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <wjf:GetPersonResponse>\n" +
                "         <return>\n" +
                "            <body>\n" +
                "               <root>\n" +
                "                  <friends>\n" +
                "                     <item>123</item>\n" +
                "                     <item>456</item>\n" +
                "                  </friends>\n" +
                "                  <friends>\n" +
                "                     <item>123</item>\n" +
                "                     <item>456</item>\n" +
                "                  </friends>\n" +
                "                  <id>444</id>\n" +
                "               </root>\n" +
                "               <root>\n" +
                "                  <friends>\n" +
                "                     <item>123</item>\n" +
                "                     <item>456</item>\n" +
                "                  </friends>\n" +
                "                  <friends>\n" +
                "                     <item>123</item>\n" +
                "                     <item>456</item>\n" +
                "                  </friends>\n" +
                "                  <id>444</id>\n" +
                "               </root>\n" +
                "            </body>\n" +
                "         </return>\n" +
                "      </wjf:GetPersonResponse>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>";
        assertEquals(responseBody,respBody);
        logger.info("responseBody={}",respBody);
        System.out.println(JsonUtils.toJSONString(httpOutput));
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


}
