package com.jd.workflow.soap.example.demo;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

public class CxfStarter {
    public static void main(String[] args) {
        try (CamelContext context = new DefaultCamelContext()) {
            context.addRoutes(new CxfRouteBuilder());
            context.start();
            context.addStartupListener(new JsonStartupMessage());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
