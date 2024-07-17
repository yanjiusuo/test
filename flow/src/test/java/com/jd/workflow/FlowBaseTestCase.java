package com.jd.workflow;

import com.jd.workflow.flow.core.camel.CamelStepProcessorFactory;
import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomLanguageResolver;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.impl.SubflowStepMetadata;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.example.FlowTransformDemo;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.flow.parser.WorkflowParser;

import com.jd.workflow.flow.parser.context.IFlowParserContext;
import com.jd.workflow.flow.parser.context.IFlowResolver;
import com.jd.workflow.flow.parser.context.impl.DefaultFlowParserContext;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.DefaultExchange;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;

public class FlowBaseTestCase extends BaseTestCase {
    public static final Logger logger = LoggerFactory.getLogger(FlowBaseTestCase.class);



    protected DefaultCamelContext newCamelContext(){
        return newCamelContext(null);
    }
   protected DefaultCamelContext newCamelContext(String routeDef){
        try{
            DefaultCamelContext camelContext = new DefaultCamelContext();
            camelContext.setLanguageResolver(new CustomLanguageResolver());
            camelContext.setProcessorFactory(new CamelStepProcessorFactory());
            if(routeDef != null){
                CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();



                logger.info("------------------------------------------------------------------");
                logger.info("def_is:def={}",routeDef);

                camelContext.addRouteDefinitions(camelRouteLoader.loadRoutes(routeDef));

            }
            return camelContext;
        }catch (Exception e){
            logger.error("camel.err_start_context",e);
            return null;
        }
    }
   protected   Exchange newExchange(DefaultCamelContext camelContext,StepContext stepContext){
        Exchange exchange = new DefaultExchange(camelContext);
        exchange.setProperty("stepContext",stepContext);
        exchange.getIn().setBody("23231221");
        exchange.setProperty("contextDef",new FlowTransformDemo());
        return exchange;
    }
    public Object  execFlow(WorkflowInput input,String jsonConfigFileName){
         return FlowTestUtils.execFlow(input,jsonConfigFileName);
    }
   protected WorkflowDefinition loadDefinition(String fileName) {
        try {

            String content = getResourceContent(fileName);
            DefaultFlowParserContext parserContext = new DefaultFlowParserContext();
            parserContext.setValidate(true);
            parserContext.setFlowResolver(new IFlowResolver() {
                @Override
                public WorkflowDefinition resolveSubflow(String entityId, SubflowStepMetadata metadata) {
                    return null;
                }
            });
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



    private static void bindBeans(Registry registry) {

    }


}
