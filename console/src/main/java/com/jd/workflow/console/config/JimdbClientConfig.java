package com.jd.workflow.console.config;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/10
 */

import com.jd.jim.cli.Cluster;
import com.jd.jim.cli.ReloadableJimClientFactory;
import com.jd.jim.cli.config.ConfigLongPollingClientFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/10
 */
@Configuration
public class JimdbClientConfig {
    /**
     *
     */
    @Value(value = "${jim.url}")
    private String jimUrl;
    /**
     *
     */
    @Value(value = "${jim.cfs:http://cfs.jim.jd.local/}")
    private String serviceEndpoint;

    @Bean(name = "factory")
    public ReloadableJimClientFactory createFactory() {
        ConfigLongPollingClientFactory configClientFactory = new ConfigLongPollingClientFactory(serviceEndpoint);
        ReloadableJimClientFactory factory = new ReloadableJimClientFactory();
        factory.setJimUrl(jimUrl);
        factory.setIoThreadPoolSize(5);
        factory.setComputationThreadPoolSize(5);
        factory.setRequestQueueSize(100000);
        factory.setConfigClient(configClientFactory.create());
        return factory;
    }

    @Bean(name = "jimClient")
    public Cluster createClient() {
        ReloadableJimClientFactory factory = createFactory();
        return factory.getClient();
    }
}
