package com.jd.workflow.soap.example.demo.routes;

import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * 根据条件确定下一步的执行
 */
public class BranchRoute extends RouteBuilder {
    @Override
    public void configure() throws Exception {


        from("direct:start")
                .doTry()
                .choice().when(new Predicate() {
                    @Override
                    public boolean matches(Exchange exchange) {
                        if(exchange.getIn().getBody() instanceof Integer){
                            return true;
                        }
                        return false;
                    }
                }).process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("int result"+exchange.getIn().getBody(Integer.class));
                    }
                })
                .otherwise()
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        System.out.println("string result"+exchange.getIn().getBody(String.class));
                    }
                });
    }

    public static void main(String[] args) throws Exception {
        BranchRoute router = new BranchRoute();
        DefaultCamelContext camelContext = new DefaultCamelContext();
        camelContext.addRoutes(router);
        camelContext.start();
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();
        producerTemplate.sendBody("direct:start",123);
        producerTemplate.sendBody("direct:start","2243");

    }
}