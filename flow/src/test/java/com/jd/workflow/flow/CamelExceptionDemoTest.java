package com.jd.workflow.flow;

import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.loader.CamelRouteLoader;
import com.jd.workflow.soap.common.exception.StdException;
import org.apache.camel.*;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.DefaultExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

public class CamelExceptionDemoTest  {
    static final Logger logger = LoggerFactory.getLogger(CamelExceptionDemoTest.class);
    //CamelNamespaceHandler.java
    public static void main(String[] args) throws IOException {
        CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();
        try {
            List<RouteDefinition> definition =  camelRouteLoader.loadRoutesFromPath("camel/camel-exception-demo.xml");

            DefaultCamelContext camelContext = new DefaultCamelContext();
            bindBeans(camelContext.getRegistry());

            camelContext.addRouteDefinitions(definition);
            camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                template.sendBody("direct:start", "absc23131");
            }
        } catch (CamelExecutionException e) {
            DefaultExchange exchange = (DefaultExchange) e.getExchange();
            Throwable cause = e.getCause();
            if(cause instanceof StepExecException){
                logger.info("exception:stepId={}",((StepExecException) cause).getStepId());
            }
            String nodeId = exchange.getHistoryNodeId();
            System.out.println(nodeId);
            e.printStackTrace();
        } catch (Exception e) {

            e.printStackTrace();
        }

    }

    private static void bindBeans(Registry registry) {
        registry.bind("logProcessor", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                DefaultExchange defaultExchange = (DefaultExchange) exchange;
                String nodeId = defaultExchange.getHistoryNodeId();
                try{
                    String input = exchange.getIn().getBody(String.class);

                    System.out.println("input::"+input+" defaultExchange:"+nodeId);
                   throw new StdException("exception0");
                }catch (Exception e){
                   StepExecException exception = new StepExecException(nodeId, e.getMessage(),e);
                    exception.setExchange(exchange);
                    throw exception;
                }
            }
        });
        registry.bind("exceptionProcessor", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                StepExecException exception = (StepExecException) exchange.getProperty(ExchangePropertyKey.EXCEPTION_CAUGHT);
                String stepId = exception.getStepId();
                logger.error("camel.exec_exception:stepId={}",stepId,exception);
            }
        });
    }

    public static String printDocument(Node doc) throws IOException, TransformerException {
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(sw));
        return sw.toString();
    }
}
