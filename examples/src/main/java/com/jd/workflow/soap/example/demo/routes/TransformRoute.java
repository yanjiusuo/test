package com.jd.workflow.soap.example.demo.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.language.MvelExpression;

public class TransformRoute extends RouteBuilder {
    static final String groovyExpression = "body = body  + \"123\";";
    static String mvelExpression = "123+properties.wjf+context.ex;";

    @Override
    public void configure() throws Exception {
        from("direct:start")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        exchange.setProperty("wjf","wjf");
                    }
                })
                .transform(simple("${body}-aaa"))

                .transform(new MvelExpression(mvelExpression))
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("body::"+exchange.getIn().getBody());
                    }
                })
                .log("${body}");
    }

    public static void main(String[] args) throws Exception {
        TransformRoute router = new TransformRoute();
        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(router);
        camelContext.start();
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start", 123);
        producerTemplate.sendBody("direct:start", "2243");


    }
}
