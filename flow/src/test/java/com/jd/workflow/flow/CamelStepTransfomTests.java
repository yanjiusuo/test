package com.jd.workflow.flow;

import com.jd.workflow.FlowBaseTestCase;
import com.jd.workflow.HttpTestServer;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.service.FullTypedWebService;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.sun.net.httpserver.HttpServer;
import org.apache.cxf.endpoint.Server;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CamelStepTransfomTests extends FlowBaseTestCase {
     @Test
     public void testCamelSimple() {
        WorkflowDefinition def = loadDefinition("classpath:flow/camel-simple.json");

        String result = RouteBuilder.buildRoute(def);
        System.out.println("def::" + result);
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

        HttpOutput output = (HttpOutput) execFlow(input, "classpath:flow/camel-complex.json");
        Map<String, Object> map = (Map<String, Object>) output.getBody();
        assertEquals("condition1", map.get("message"));

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
            HttpOutput output = (HttpOutput) execFlow(input, "classpath:flow/camel-all-step-complex.json");
            //StepContext context = (StepContext) output.attr("context");
            //logger.info("execDetail:{}",JsonUtils.toJSONString(context.toLog()));
            Map<String, Object> map = (Map<String, Object>) output.getBody();
            assertEquals("{\"code\":\"200\",\"data\":{\"res1\":\"condition1\",\"res2\":{\"wsRes\":{\"retValue\":{\"id\":1,\"name\":\"body\"}},\"http1Res\":{\"name\":\"body\",\"id\":\"111\"}}},\"message\":\"condition1\"}",JsonUtils.toJSONString(map));
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

        HttpOutput output = (HttpOutput) execFlow(input, "classpath:http/exception-collect.json");
        Map<String,Object> map = (Map<String, Object>) output.getBody();
        assertEquals("This is the response",map.get("data"));

        logger.info("result_is:result={}", JsonUtils.toJSONString(output));
        server.stop(0);
    }

}
