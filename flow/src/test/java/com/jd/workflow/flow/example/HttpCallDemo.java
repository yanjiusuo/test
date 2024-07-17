package com.jd.workflow.flow.example;

import com.jd.workflow.flow.loader.CamelRouteLoader;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.reifier.ProcessorReifier;

import java.util.Collections;
import java.util.List;

/**
 * https://camel.apache.org/components/3.16.x/http-component.html
 */
public class HttpCallDemo {
    public static void main(String[] args) {
        CamelRouteLoader camelRouteLoader  = new CamelRouteLoader();
        try {
            List<RouteDefinition> definitions =  camelRouteLoader.loadRoutesFromPath("route/http-route.xml");

            DefaultCamelContext camelContext = new DefaultCamelContext();

            camelContext.addRouteDefinitions(definitions);

            ProcessorReifier refier = null;

            camelContext.start();
            try (ProducerTemplate template = camelContext.createProducerTemplate()) {
                template.sendBody("direct:start", "absc23131");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
