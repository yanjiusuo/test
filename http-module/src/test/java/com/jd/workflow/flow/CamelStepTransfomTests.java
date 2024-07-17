package com.jd.workflow.flow;

import com.jd.workflow.BaseTestCase;

import com.jd.workflow.HttpTestServer;
import com.jd.workflow.flow.core.camel.CamelStepProcessorFactory;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.expr.CustomLanguageResolver;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.service.FullTypedWebService;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.sun.net.httpserver.HttpServer;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.support.DefaultExchange;
import org.apache.cxf.endpoint.Server;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class CamelStepTransfomTests extends BaseTestCase {
     @Test
     public void testCamelSimple() {
        WorkflowDefinition def = FlowTestUtils.loadDefinition("classpath:flow/camel-simple.json");

        String result = RouteBuilder.buildRoute(def);
        System.out.println("def::" + result);
    }
    private DefaultCamelContext startRoute(String routePath) throws Exception {
        CamelRouteLoader camelRouteLoader = new CamelRouteLoader();

        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.setLanguageResolver(new CustomLanguageResolver());
        camelContext.setProcessorFactory(new CamelStepProcessorFactory());

        camelContext.addRouteDefinitions(camelRouteLoader.loadRoutesFromPath(routePath));
        //camelContext.build();
        camelContext.start();
        return camelContext;
    }
    @Test public void testIfHttp() throws Exception {

        DefaultCamelContext camelContext = startRoute("camel/if-camel-step-bean.xml");


        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        workflowInput.setParams(params);


        StepContext stepContext = new StepContext();
        stepContext.setInput(workflowInput);


        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty("stepContext", stepContext);
        try (ProducerTemplate template = camelContext.createProducerTemplate()) {
            template.send("direct:start", exchange);
            //template.send()

        }
        Object result = exchange.getMessage().getBody();
        System.out.println(result);

    }
    @Test public void testSingleFlowStep() throws IOException {
        HttpServer server = HttpTestServer.run(6010);
        WorkflowInput workflowInput = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("pageNo",1);
        workflowInput.setParams(params);

        Map<String,Object> body = new LinkedHashMap<>();
        body.put("sid",123);
        body.put("name","name");

        workflowInput.setBody(body);

        HttpOutput result = (HttpOutput) FlowTestUtils.execFlow(workflowInput,"classpath:flow/single-flow.json");
        assertEquals("{\"sid\":123,\"name\":\"name\"}",JsonUtils.toJSONString(result.getBody()));
        server.stop(0);
    }
    /**
     * 包含 分支、判断、if
     */
     @Test public void testCamelComplex() throws Exception {
        HttpServer server = HttpTestServer.run(6010);
        WorkflowInput input = new WorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("id", "1");
        input.setParams(params);

        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(input, "classpath:flow/camel-complex.json");
        Map<String, Object> map = (Map<String, Object>) output.getBody();
        Assert.assertEquals("condition1", map.get("message"));

        logger.info("result_is:result={}", JsonUtils.toJSONString(output));
        server.stop(0);

    }
     @Test public void testCamelComplexDemo() throws Exception {
        HttpServer server = HttpTestServer.run(6010);
         Server webserviceServer = null;
        WorkflowInput input = new WorkflowInput();
        Map<String, Object> body = new HashMap<>();
        body.put("id", "1");
        body.put("b1", "1");
        body.put("name", "body");
        input.setBody(body);
        try{
            input.getHeaders().put("a1",1);
            input.getHeaders().put("a2",2);

             webserviceServer = FullTypedWebService.run(null, "http://127.0.0.1:7001/FullTypedWebService");
            HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(input, "classpath:flow/camel-all-step-complex.json");
            //StepContext context = (StepContext) output.attr("context");
            //logger.info("execDetail:{}",JsonUtils.toJSONString(context.toLog()));
            Map<String, Object> map = (Map<String, Object>) output.getBody();
            Assert.assertEquals("{\"code\":\"200\",\"data\":{\"res1\":\"condition1\",\"res2\":{\"http1Res\":{\"name\":\"body\",\"id\":\"111\"},\"wsRes\":{\"retValue\":{\"id\":1,\"name\":\"body\"}}}},\"message\":\"condition1\"}",JsonUtils.toJSONString(map));
            logger.info("result_is:result={}", JsonUtils.toJSONString(output));

        }finally {
            webserviceServer.stop();
            server.stop(0);
        }

    }
     @Test public void testExceptionCollect() throws IOException {
        HttpServer server = HttpTestServer.run(6010);

        WorkflowInput input = new WorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("id", "1");
        input.setParams(params);

        HttpOutput output = (HttpOutput) FlowTestUtils.execFlow(input, "classpath:http/exception-collect.json");
        Map<String,Object> map = (Map<String, Object>) output.getBody();
        Assert.assertEquals("This is the response",map.get("data"));

        logger.info("result_is:result={}", JsonUtils.toJSONString(output));
        server.stop(0);
    }

}
