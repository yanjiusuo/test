package com.jd.workflow.flow;

import com.jd.workflow.FlowBaseTestCase;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.impl.SubflowStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.processor.subflow.CamelSubflowProcessor;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.flow.parser.context.IFlowResolver;
import com.jd.workflow.flow.parser.context.impl.DefaultFlowParserContext;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubflowTests extends FlowBaseTestCase {
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
     public void testEasySubFlow() throws Exception {

         WorkflowInput input = new WorkflowInput();
         Map<String,Object> params = new HashMap<>();
         params.put("id","1");
         input.setParams(params);
         Output output = startFlow("classpath:subflow/subflow.json",input);
         assertEquals("1",output.getBody());
         logger.info("result_is:result={}",JsonUtils.toJSONString(output));
    }
    @Test
    public void testExceptionSubflow() throws Exception {
        WorkflowInput input = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("id","1");
        input.setParams(params);
        Output output = startFlow("classpath:subflow/exception_subflow.json",input);
        assertEquals("-1",output.getBody());
        logger.info("result_is:result={}",JsonUtils.toJSONString(output));
    }
    @Test
    public void testPreProcess() throws Exception {
        WorkflowInput input = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("id","1");
        input.setParams(params);
        Output output = startFlow("classpath:subflow/prescript.json",input);
        assertEquals("1",output.getBody());
        logger.info("result_is:result={}",JsonUtils.toJSONString(output));
    }
    @Test
    public void testScript() throws Exception {
        WorkflowInput input = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("id","1");
        input.setParams(params);
        Output output = startFlow("classpath:subflow/script.json",input);
        assertEquals("1",output.getBody());
        logger.info("result_is:result={}",JsonUtils.toJSONString(output));
    }
    @Test
    public void testSubflowSubflow() throws Exception {

    }
    private WorkflowDefinition loadDef(Long id ){
        String content = getResourceContent("classpath:subflow/base_sub_flow.json");
        return WorkflowParser.parse(content);
    }
    @Test
    public void testSubFlowDef() throws Exception {
        WorkflowInput input = new WorkflowInput();
        Map<String,Object> params = new HashMap<>();
        params.put("id","1");
        input.setParams(params);

        DefaultFlowParserContext parserContext = new DefaultFlowParserContext();
        parserContext.setValidate(true);
        parserContext.pushFlowId("321");
        parserContext.setFlowResolver(new IFlowResolver() {
            @Override
            public WorkflowDefinition resolveSubflow(String entityId, SubflowStepMetadata metadata) {
                Long id = Long.valueOf(entityId);
                return loadDef(id);
            }
        });
        //Map<String,Object> def = JsonUtils.parse(defStr, Map.class);
        Map<String,Object> map = JsonUtils.parse(getResourceContent("classpath:subflow/parent_flow.json"),Map.class);
        WorkflowDefinition definition = WorkflowParser.parse(map,parserContext);

        parserContext.removeFlowId("321");


        CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();


        String def  = RouteBuilder.buildRoute(definition);
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

            assertEquals("1",output.getBody());

        }




    }

}
