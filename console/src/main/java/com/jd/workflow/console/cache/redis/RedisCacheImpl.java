package com.jd.workflow.console.cache.redis;

import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.serializer.DefaultSerializer;
import com.jd.workflow.soap.common.cache.AbstractCache;
import com.jd.workflow.soap.common.cache.ICache;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import com.jd.jim.cli.Cluster;
import org.apache.commons.lang3.StringUtils;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RedisCacheImpl extends AbstractCache {

    RedisTemplate redisTemplate;
    private final static DefaultSerializer DEFAULT_SERIALIZER = new DefaultSerializer();

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void hSet(String key, String field, Object obj, int seconds) {
        String cachedKey = cachedKey(key,field);
      /*  byte[] byteKey = DEFAULT_SERIALIZER.serialize(cachedKey);
         byte[] obj1 = DEFAULT_SERIALIZER.serialize(obj);*/


        log.info("cache.hset:key={},field={},obj={}" , key, field ,  JsonUtils.toJSONString(obj));
       // client.setex(byteKey,Long.valueOf(seconds),obj1);
        redisTemplate.opsForValue().set(cachedKey,obj,seconds, TimeUnit.SECONDS);
    }

    @Override
    public <T> T hGet(String key, String field) {
        log.info("cache.get:key={},field={}" , key,field);
        if (StringUtils.isBlank(key)) return null;

        return (T) redisTemplate.opsForValue().get(cachedKey(key,field));
    }

    @Override
    public <T> T hRemove(String key, String field) {
        byte[] byteKey = DEFAULT_SERIALIZER.serialize(cachedKey(key,field));

        redisTemplate.delete(key);
        return null;
    }

}
