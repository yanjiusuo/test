package com.jd.workflow.soap.example.demo.routes;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.spi.Registry;
import org.apache.camel.support.DefaultExchange;

import java.util.Arrays;

/**
 * 按照key进行结果聚集操作
 */
public class AggByKeyRoute extends RouteBuilder {
    protected static void bindBeans(Registry registry) throws Exception {

        registry.bind("waiter", new Waiter());
    }

    static class Waiter{
        public int check(String result) {
            System.out.println(result);
            String[] strs = new String[]{"a","b","c"};
            if(Arrays.asList(strs).contains(result)){
                return 1;
            }
            return 2;
        }

    }
    @Override
    public void configure() throws Exception {
        from("direct:start")

                .aggregate(new AggregationStrategy() {
                    @Override
                    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
                        String oldMsg = oldExchange == null ? "" : oldExchange.getIn().getBody(String.class);
                        String msg2 = newExchange == null ? "" : newExchange.getIn().getBody(String.class);
                        newExchange.getIn().setBody(oldMsg+msg2);
                        return newExchange;
                    }
                }).simple("end").completionTimeout(2000)
                .to("direct:to");
        from("direct:to").process(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {
                System.out.println(exchange.getIn().getBody());
            }
        }).log("result:${body}");

    }

    public static void main(String[] args) throws Exception {
        AggByKeyRoute router = new AggByKeyRoute();
        DefaultCamelContext camelContext = new DefaultCamelContext();
        bindBeans(camelContext.getRegistry());
        camelContext.addRoutes(router);
        camelContext.start();
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        DefaultExchange defaultExchange = new DefaultExchange(camelContext);

        producerTemplate.send("direct:start",defaultExchange);

        while (true);
    }
}