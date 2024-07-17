package com.jd.workflow.soap.example.demo;

import org.apache.camel.builder.RouteBuilder;
import com.jd.workflow.soap.example.custom.WebServiceProcessor;

public class CxfRouteBuilder extends RouteBuilder {
    @Override
    public void configure() throws Exception {
        from("direct:start")
                .process(new WebServiceProcessor())
                .log("Response: ${id}:${body}");;
    }
}
