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

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MulticastRouteTests extends HttpBaseTestCase {

     @Test
     public void testParamValidate() throws Exception {
        WorkflowDefinition workflowDef = FlowTestUtils.loadDefinition("classpath:multicast/multicast-demo.json");

        CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();


            String def  = RouteBuilder.buildRoute(workflowDef);
            logger.info("------------------------------------------------------------------");
            logger.info("def_is:def={}",def);
            RouteDefinition definition =  camelRouteLoader.loadRoute(def);


            DefaultCamelContext camelContext = FlowTestUtils.newCamelContext();


            camelContext.addRouteDefinitions(Collections.singletonList(definition));
        camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                StepContext stepContext = new StepContext();


                WorkflowInput input = new WorkflowInput();


                stepContext.setInput(input);
                Exchange exchange = FlowTestUtils.newExchange(camelContext,stepContext);
                template.send("direct:start", exchange);

                HttpOutput output = (HttpOutput) exchange.getIn().getBody();
                Map<String,Object> body = (Map<String, Object>) output.getBody();
                assertEquals( "This is the response",body.get("http1Res"));
                assertEquals( "{\"name\":\"name\",\"sid\":12}",JsonUtils.toJSONString(body.get("http2Res")));
            }

    }
}
