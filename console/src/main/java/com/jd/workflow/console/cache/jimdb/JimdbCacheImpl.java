package com.jd.workflow.console.cache.jimdb;

import com.jd.jim.cli.Cluster;
import com.jd.workflow.console.cache.redis.RedisCacheImpl;
import com.jd.workflow.console.cache.redis.RedisConfig;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.console.serializer.DefaultSerializer;
import com.jd.workflow.soap.common.cache.AbstractCache;
import com.jd.workflow.soap.common.cache.ICache;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

@Slf4j
public class JimdbCacheImpl extends AbstractCache {
    private final static DefaultSerializer DEFAULT_SERIALIZER = new DefaultSerializer();
    private Cluster client;

    public void setClient(Cluster client) {
        this.client = client;
    }

    @Override
    public void hSet(String key, String field, Object obj, int seconds) {
        String cachedKey = cachedKey(key,field);
        byte[] byteKey = DEFAULT_SERIALIZER.serialize(cachedKey);
        byte[] obj1 = DEFAULT_SERIALIZER.serialize(obj);


        log.info("cache.hset:key={},field={},obj={}" , key, field ,  JsonUtils.toJSONString(obj));
        client.setEx(byteKey,obj1,Long.valueOf(seconds), TimeUnit.SECONDS);
    }

    @Override
    public <T> T hGet(String key, String field) {
        log.info("cache.get:key={},field={}" , key,field);
        if (StringUtils.isBlank(key)) return null;
        byte[] byteKey = DEFAULT_SERIALIZER.serialize(cachedKey(key,field));
        byte[] res = client.get(byteKey);
        return res == null ? null : (T) DEFAULT_SERIALIZER.deserialize(res);
    }

    @Override
    public <T> T hRemove(String key, String field) {
        byte[] byteKey = DEFAULT_SERIALIZER.serialize(cachedKey(key,field));
        Long del = client.del(byteKey);
        return null;
    }


}
