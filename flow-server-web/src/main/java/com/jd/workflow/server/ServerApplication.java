package com.jd.workflow.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(scanBasePackages = {"com.jd"})
@ServletComponentScan
@EnableCaching
@ImportResource(value = {"classpath:spring/spring.xml", "classpath:spring/spring-runtime.xml"})
public class ServerApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ServerApplication.class);
    }
}