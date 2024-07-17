package com.jd.workflow.console;

import com.jd.security.watermark.docwatermarksdk.DocWatermarkSdkAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(scanBasePackages = {"com.jd"},exclude = {
        RedisAutoConfiguration.class,
        RedisRepositoriesAutoConfiguration.class, DocWatermarkSdkAutoConfiguration.class
})
@ServletComponentScan
@EnableCaching
@ImportResource({"classpath:spring/spring.xml"}) //,"jtfm-configcenter-ducc.properties"
public class ConsoleApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        SpringApplication.run(ConsoleApplication.class, args);

    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(ConsoleApplication.class);
    }
}