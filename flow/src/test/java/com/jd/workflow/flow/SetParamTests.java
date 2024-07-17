package com.jd.workflow.flow;

import com.jd.workflow.FlowBaseTestCase;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.subflow.CamelSubflowProcessor;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SetParamTests extends FlowBaseTestCase {
    public Output startFlow(String stepDef, WorkflowInput input) throws Exception {
        WorkflowDefinition workflowDef = loadDefinition(stepDef);

        CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();


        String def  = RouteBuilder.buildRoute(workflowDef);
        logger.info("------------------------------------------------------------------");
        logger.info("def_is:def={}",def);
        List<RouteDefinition> definitions =  camelRouteLoader.loadRoutes(def);


        DefaultCamelContext camelContext = newCamelContext();



        camelContext.addRouteDefinitions(definitions);
        camelContext.start();
        try (ProducerTemplate template = camelContext.createProducerTemplate()) {
            StepContext stepContext = new StepContext();
            stepContext.setInput(input);
            stepContext.setSubflowProcessor(new CamelSubflowProcessor(template));
            Exchange exchange = newExchange(camelContext,stepContext);
            template.send("direct:start", exchange);

            HttpOutput output = (HttpOutput) exchange.getMessage().getBody();
            logger.info("result_is:result={}",JsonUtils.toJSONString(output));
            return output;
        }
    }
    @Test
    public void testSetParam() throws Exception {

        WorkflowInput input = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("id","1");
        input.setParams(params);
        Output output = startFlow("classpath:flow/set-param-flow.json",input);
        assertEquals("{\"name\":\"wjf\",\"sid\":\"1\"}",JsonUtils.toJSONString(output.getBody()));
        logger.info("result_is:result={}", JsonUtils.toJSONString(output));
    }

}
