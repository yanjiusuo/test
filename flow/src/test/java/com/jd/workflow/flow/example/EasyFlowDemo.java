package com.jd.workflow.flow.example;

import com.jd.workflow.flow.loader.CamelRouteLoader;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.Registry;
import org.w3c.dom.Node;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Collections;
import java.util.List;

public class EasyFlowDemo {




    //CamelNamespaceHandler.java
    public static void main(String[] args) throws IOException {
        CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();
        try {
            List<RouteDefinition> definitions =  camelRouteLoader.loadRoutesFromPath("route/log-route.xml");

            DefaultCamelContext camelContext = new DefaultCamelContext();
            bindBeans(camelContext.getRegistry());

            camelContext.addRouteDefinitions(definitions);
            camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                template.sendBody("direct:start", "absc23131");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void bindBeans(Registry registry) {
        registry.bind("logProcessor", new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                String input = exchange.getIn().getBody(String.class);
                System.out.println("input::"+input);
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
