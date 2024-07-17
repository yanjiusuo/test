package com.jd.workflow.console.service.doc.app;

import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

//,"springfox"
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class, DruidDataSourceAutoConfigure.class},
        scanBasePackages = {"com.jd.workflow.console.service.doc.app","springfox"})

public class TestApplication {
    static final Logger log = LoggerFactory.getLogger(TestApplication.class);
    public static void main(String[] args) {

        log.info("Begin to start Spring Boot up-standardserve Application");
        long startTime = System.currentTimeMillis();


        SpringApplication.run(TestApplication.class);


        long endTime = System.currentTimeMillis();
        log.info("End starting Spring Boot up-standardserve Application, Time used: "+ (endTime - startTime) );
    }
}
