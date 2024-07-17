package com.jd.workflow.console.cache.redis;

import com.jd.common.util.StringUtils;
import com.jd.workflow.soap.common.cache.ICache;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
@ConditionalOnProperty(value = "cache.cacheImpl",havingValue = "redis")
public class RedisConfig {
    @Value("${redis.pool.host}")
    String redisHost;
    @Value("${redis.pool.port}")
    Integer redisPort;
    @Value("${redis.pool.pass}")
    String redisPass;
    @Value("${redis.pool.timeout}")
    Integer redisTimeout=2000;

    public void setRedisHost(String redisHost) {
        this.redisHost = redisHost;
    }

    public void setRedisPort(Integer redisPort) {
        this.redisPort = redisPort;
    }

    public void setRedisPass(String redisPass) {
        this.redisPass = redisPass;
    }

    public void setRedisTimeout(Integer redisTimeout) {
        this.redisTimeout = redisTimeout;
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMinIdle(10);
        config.setMaxWaitMillis(5000);
        config.setNumTestsPerEvictionRun(10);
        config.setTestOnReturn(true);
        config.setSoftMinEvictableIdleTimeMillis(60000);
        return config;
    }
    @Bean
    public JedisConnectionFactory jedisConnectionFactory(JedisPoolConfig poolConfig){
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setPoolConfig(poolConfig);
        factory.setHostName(redisHost);
        factory.setPort(redisPort);
        if(StringUtils.isNotBlank(redisPass)){
            factory.setPassword(redisPass);
        }

        if(redisTimeout != null){
            factory.setTimeout(redisTimeout);
        }

        factory.setUsePool(true);
        return factory;
    }
    @Bean
    public RedisTemplate redisTemplate(JedisConnectionFactory jedisConnectionFactory){
        RedisTemplate redisTemplate = new RedisTemplate();
        redisTemplate.setConnectionFactory(jedisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new JdkSerializationRedisSerializer());

        return redisTemplate;
    }
    @Bean
    public ICache redisCache(RedisTemplate redisTemplate){
        RedisCacheImpl cache = new RedisCacheImpl();
        cache.setRedisTemplate(redisTemplate);
        return cache;
    }

}
