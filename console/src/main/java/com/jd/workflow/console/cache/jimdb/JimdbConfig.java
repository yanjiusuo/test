package com.jd.workflow.console.cache.jimdb;

import com.jd.jim.cli.Cluster;
import com.jd.jim.cli.ReloadableJimClientFactory;
import com.jd.jim.cli.config.ConfigLongPollingClientFactory;
import com.jd.workflow.soap.common.cache.ICache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@Slf4j
@ConditionalOnProperty(value = "cache.cacheImpl",havingValue = "jimdb")
public class JimdbConfig {
    @Value("redis.jimUrl")
    String jimUrl;

    public void setJimUrl(String jimUrl) {
        this.jimUrl = jimUrl;
    }

    @Bean
    public ReloadableJimClientFactory jimdbClientFactory(){
        ConfigLongPollingClientFactory configClientFactory = new ConfigLongPollingClientFactory();
        ReloadableJimClientFactory factory = new ReloadableJimClientFactory();
        factory.setJimUrl(jimUrl);
        factory.setIoThreadPoolSize(5);
        factory.setComputationThreadPoolSize(5);
        factory.setRequestQueueSize(100000);
        factory.setConfigClient(configClientFactory.create());
        return factory;
    }

    @Bean
    public Cluster jimdbClient(ReloadableJimClientFactory jimdbClientFactory){
        try {

            log.info("=======> Init upgrade redis success");
            return jimdbClientFactory.getClient();
        }catch (Exception e){
            log.error("=======> Init upgrade redis fail message:{}", e);
            throw e;
        }
    }

    @Bean
    public ICache jimdbCache(Cluster jimdbClient){
        JimdbCacheImpl cache = new JimdbCacheImpl();
        cache.setClient(jimdbClient);
        return cache;
    }

}
