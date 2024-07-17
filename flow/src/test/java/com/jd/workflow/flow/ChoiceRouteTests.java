package com.jd.workflow.flow;

import com.jd.workflow.FlowBaseTestCase;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.utils.StepContextHelper;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.ManagementInterceptStrategy;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ChoiceRouteTests extends FlowBaseTestCase {
     @Test
     public void testChoiceBuilder(){
        WorkflowDefinition workflowDef = loadDefinition("classpath:choice/choice-def.json");

        CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();
        try {

            String def  = RouteBuilder.buildRoute(workflowDef);
            logger.info("------------------------------------------------------------------");
            logger.info("def_is:def={}",def);
            RouteDefinition definition =  camelRouteLoader.loadRoute(def);


            DefaultCamelContext camelContext = newCamelContext();



            camelContext.addRouteDefinitions(Collections.singletonList(definition));
            camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                StepContext stepContext = new StepContext();


                WorkflowInput input = new WorkflowInput();
                Map<String,Object> params = new HashMap<>();
                params.put("id","1");
                input.setParams(params);
                stepContext.setInput(input);

                Exchange exchange = newExchange(camelContext,stepContext);
                template.send("direct:start", exchange);

                HttpOutput output = (HttpOutput) exchange.getMessage().getBody();
                assertEquals("condition1",output.getBody());
                logger.info("result_is:result={}",JsonUtils.toJSONString(output));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
     @Test public void testException(){

        WorkflowDefinition workflowDef = loadDefinition("classpath:choice/choice-exception-def.json");

        CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();
        try {

            String def  = RouteBuilder.buildRoute(workflowDef);
            logger.info("------------------------------------------------------------------");
            logger.info("def_is:def={}",def);
            RouteDefinition definition =  camelRouteLoader.loadRoute(def);


            DefaultCamelContext camelContext = newCamelContext();



            camelContext.addRouteDefinitions(Collections.singletonList(definition));

            try{

                camelContext.start();

            }catch (Exception e){
                logger.error("error_create_context",e);
            }

            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                StepContext stepContext = new StepContext();


                WorkflowInput input = new WorkflowInput();
                Map<String,Object> params = new HashMap<>();
                params.put("id","1");
                input.setParams(params);
                stepContext.setInput(input);

                Exchange exchange = newExchange(camelContext,stepContext);
                template.send("direct:start", exchange);

                HttpOutput output = (HttpOutput) exchange.getMessage().getBody();
                assertEquals(400,output.getStatus());
                logger.info("result_is:result={}",JsonUtils.toJSONString(output));

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
