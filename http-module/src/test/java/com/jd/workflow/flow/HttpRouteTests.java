package com.jd.workflow.flow;

import com.jd.workflow.HttpBaseTestCase;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.Registry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class HttpRouteTests extends HttpBaseTestCase {
    static final Logger logger = LoggerFactory.getLogger(HttpRouteTests.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();

    }

    WorkflowInput newWorkflowInput() {
        WorkflowInput input = new WorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("pageNo", 1);
        params.put("pageSize", 10);
        input.setParams(params);
        return input;
    }

    private static void bindBeans(Registry registry) {
        //registry.bind("httpProcessor", new HttpProcessor());
    }


    @Test
    public void testParamValidate() throws Exception {
        WorkflowDefinition workflowDef = FlowTestUtils.loadDefinition("classpath:http/http-flow-demo.json");

        CamelRouteLoader camelRouteLoader = new CamelRouteLoader();


        String def = RouteBuilder.buildRoute(workflowDef);
        logger.info("------------------------------------------------------------------");
        logger.info("def_is:def={}", def);
        RouteDefinition definition = camelRouteLoader.loadRoute(def);


        DefaultCamelContext camelContext = FlowTestUtils.newCamelContext();
        camelContext.addRouteDefinitions(Collections.singletonList(definition));
        camelContext.start();
        try (ProducerTemplate template = camelContext.createProducerTemplate()) {
            StepContext stepContext = new StepContext();

            Map<String, Object> headers = new HashMap<>();
            headers.put("token", "213");

            WorkflowInput input = new WorkflowInput();
            input.setHeaders(headers);

            stepContext.setInput(input);
            Exchange exchange = FlowTestUtils.newExchange(camelContext, stepContext);
            template.send("direct:start", exchange);

            HttpOutput output = (HttpOutput) exchange.getIn().getBody();

            assertEquals("This is the response", output.getBody());
        }

    }

    @Test
    public void testErrorResponse() {
        WorkflowDefinition workflowDef = FlowTestUtils.loadDefinition("classpath:http/http-flow-error.json");

        CamelRouteLoader camelRouteLoader = new CamelRouteLoader();
        try {

            String def = RouteBuilder.buildRoute(workflowDef);
            logger.info("------------------------------------------------------------------");
            logger.info("def_is:def={}", def);
            RouteDefinition definition = camelRouteLoader.loadRoute(def);


            DefaultCamelContext camelContext = FlowTestUtils.newCamelContext();
            camelContext.addRouteDefinitions(Collections.singletonList(definition));
            camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                StepContext stepContext = new StepContext();


                WorkflowInput input = new WorkflowInput();

                stepContext.setInput(input);
                Exchange exchange = FlowTestUtils.newExchange(camelContext, stepContext);
                template.send("direct:start", exchange);

                HttpOutput output = (HttpOutput) exchange.getIn().getBody();
                assertEquals(400, output.getStatus());
                //assertEquals( "This is the response",output.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testErrorScript() {
        WorkflowDefinition workflowDef = FlowTestUtils.loadDefinition("classpath:http/http-flow-error-script.json");

        CamelRouteLoader camelRouteLoader = new CamelRouteLoader();
        try {

            String def = RouteBuilder.buildRoute(workflowDef);
            logger.info("------------------------------------------------------------------");
            logger.info("def_is:def={}", def);
            RouteDefinition definition = camelRouteLoader.loadRoute(def);


            DefaultCamelContext camelContext = FlowTestUtils.newCamelContext();
            camelContext.addRouteDefinitions(Collections.singletonList(definition));
            camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                StepContext stepContext = new StepContext();


                WorkflowInput input = new WorkflowInput();

                stepContext.setInput(input);
                Exchange exchange = FlowTestUtils.newExchange(camelContext, stepContext);
                template.send("direct:start", exchange);

                HttpOutput output = (HttpOutput) exchange.getIn().getBody();
                assertEquals(200, output.getStatus());
                assertEquals(1, output.getHeaders().get("a"));
                assertEquals("{\"a\":1}", JsonUtils.toJSONString(output.getBody()));
                //assertEquals( "This is the response",output.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testFallback() {

    }
}
