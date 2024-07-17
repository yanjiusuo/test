package com.jd.workflow.console.cache.memory;

import com.jd.workflow.console.cache.redis.RedisCacheImpl;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.soap.common.cache.ICache;
import com.jd.workflow.soap.common.cache.impl.MemoryCache;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(value = "cache.cacheImpl",havingValue = "memory")
public class MemoryCacheConfig {
    @Bean
    public ICache redisCache(){
        MemoryCache cache = new MemoryCache();

        return cache;
    }


}
