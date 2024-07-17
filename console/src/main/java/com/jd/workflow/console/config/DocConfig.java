package com.jd.workflow.console.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

@Configuration
public class DocConfig {
    @Value("${threadpool.corePoolSize.docReport:8}")
    private Integer docReportCorePoolSize;
    @Bean(name = "docThreadExecutor")
    public ScheduledThreadPoolExecutor degradedThreadPoolExecutor() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("docReportThread").build();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(docReportCorePoolSize, namedThreadFactory);

        executor.setMaximumPoolSize(docReportCorePoolSize);
        //executor.setRejectedExecutionHandler(new DefaultRejectExecuteHandler());
        return executor;
    }


    @Bean(name = "statisticExecutor")
    public ScheduledThreadPoolExecutor statisticExecutor() {
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("statisticExecutor").build();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(4, namedThreadFactory);

        executor.setMaximumPoolSize(4);
        //executor.setRejectedExecutionHandler(new DefaultRejectExecuteHandler());
        return executor;
    }
}
