package com.jd.workflow.flow.utils;

import com.jd.workflow.flow.core.camel.CamelStepProcessorFactory;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomLanguageResolver;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.step.StepContext;

import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.support.DefaultExchange;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;

/**
 * 专门用来做与流程测试相关的工具类
 */
public class FlowTestUtils {
    public static final Logger logger = LoggerFactory.getLogger(FlowTestUtils.class);
    public static DefaultCamelContext newCamelContext(){
        return newCamelContext(null);
    }
    public static DefaultCamelContext newCamelContext(String routeDef){
        try{
            DefaultCamelContext camelContext = new DefaultCamelContext();
            camelContext.setLanguageResolver(new CustomLanguageResolver());
            camelContext.setProcessorFactory(new CamelStepProcessorFactory());
            if(routeDef != null){
                CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();



                logger.info("------------------------------------------------------------------");
                logger.info("def_is:def={}",routeDef);
                RouteDefinition definition =  camelRouteLoader.loadRoute(routeDef);
                camelContext.addRouteDefinitions(Collections.singletonList(definition));

            }
            return camelContext;
        }catch (Exception e){
            logger.error("camel.err_start_context",e);
            return null;
        }
    }
    public static   Exchange newExchange(DefaultCamelContext camelContext,StepContext stepContext){
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty("stepContext",stepContext);

        return exchange;
    }
    public static Object  execFlowByContent(WorkflowInput input, String content){
        WorkflowDefinition workflowDef = null;
        try {

            workflowDef = WorkflowParser.parse(content);

        }catch (StepParseException e) {

            logger.error("error_load_workflow:msg={}", ErrorMessageFormatter.formatMsg(e), e);

            throw e;
        } catch (Exception e) {

            logger.error("error_load_workflow", e);

            throw StdException.adapt(e);
        }

        CamelRouteLoader camelRouteLoader = new CamelRouteLoader();
        try {
            String routeXml = RouteBuilder.buildRoute(workflowDef); // o

            DefaultCamelContext camelContext = newCamelContext();

            // RouteDefinition definition =  camelRouteLoader.loadRoute("route/cast-route.xml");

            StepContext stepContext = new StepContext();
            stepContext.setInput(input);
            logger.info("route.loadXml={}",routeXml);


            camelContext.addRouteDefinitions(Collections.singletonList(camelRouteLoader.loadRoute(routeXml)));
            camelContext.start();
            Exchange exchange = new DefaultExchange(camelContext);
            exchange.setProperty("stepContext",stepContext);

            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                template.send("direct:start", exchange);
                //template.send()

            }
            Output result = (Output) exchange.getMessage().getBody();
            // result.attr("context",stepContext);
            logger.info("flow.exec_logs:{}", JsonUtils.toJSONString(stepContext.toLog()));
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static Object  execFlow(WorkflowInput input, String jsonConfigFileName){
        String content = getResourceContent(jsonConfigFileName);
        return execFlowByContent(input,content);
    }
    public static WorkflowDefinition loadDefinition(String fileName) {
        try {

            String content = getResourceContent(fileName);
            WorkflowDefinition def = WorkflowParser.parse(content);
            return def;
        }catch (StepParseException e) {

            logger.error("error_load_workflow:msg={}", ErrorMessageFormatter.formatMsg(e), e);

            throw e;
        } catch (Exception e) {

            logger.error("error_load_workflow", e);

            throw StdException.adapt(e);
        }
    }
    public static String getResourceContent(String path){

        try {
            File file = ResourceUtils.getFile(path);
            return IOUtils.toString(new FileInputStream(file),"utf-8");
        } catch (Exception e) {
            return null;
        }
    }
}
