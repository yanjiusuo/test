package com.jd.workflow.console.cache;

import com.jd.workflow.console.cache.jimdb.JimdbConfig;
import com.jd.workflow.console.cache.redis.RedisCacheImpl;
import com.jd.workflow.console.cache.redis.RedisConfig;
import com.jd.workflow.console.entity.UserInfo;
import com.jd.workflow.soap.common.cache.ICache;
import com.jd.workflow.soap.common.cache.impl.MemoryCache;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

@RunWith(JUnit4.class)
public class CacheTests extends Assert {
    @Test
    public void testRedisCache(){
        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setRedisHost("127.0.0.1");
        redisConfig.setRedisPort(6379);
        JedisConnectionFactory factory = redisConfig.jedisConnectionFactory(redisConfig.jedisPoolConfig());
        factory.afterPropertiesSet();
        RedisTemplate redisTemplate = redisConfig.redisTemplate(factory);
        redisTemplate.afterPropertiesSet();
        RedisCacheImpl cache = (RedisCacheImpl) redisConfig.redisCache(redisTemplate);
        cacheTest(cache);
    }
    @Test
    public void testMemoryCache(){

        cacheTest(new MemoryCache());
    }
    @Test
    public void testjimdbCache(){
        JimdbConfig redisConfig = new JimdbConfig();
        redisConfig.setJimUrl("jim://2914173422341158041/110000259");
        ICache cache = redisConfig.jimdbCache(redisConfig.jimdbClient(redisConfig.jimdbClientFactory()));




        cacheTest(cache);
    }
    @Test
    public void checkExpireTest(){

            MemoryCache cache = new MemoryCache();
            cache.hSet("wjf","1",123,1);
            cache.hSet("wjf","2",123,1);
            cache.hSet("wjf","3",123,1);
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cache.hGet("wjf","123");
            assertTrue(cache.isEmpty());
    }

    private void cacheTest(ICache cache){
        //Jedis jedis = new Jedis("127.0.0.1", 6379);
        //System.out.println(jedis.ping());
        int seconds = 5;
        UserInfo value = new UserInfo();
        value.setId(213L);
        cache.hSet("wjf","test",value, seconds);
        Object o = cache.hGet("wjf", "test");
        Assert.assertEquals(value,o);
        Object o1 = cache.hRemove("wjf", "test");
        Assert.assertNull(cache.hGet("wjf", "test"));

        cache.hSet("wjf","dd","123",1);
        try {
            Thread.sleep(1001);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertNull(cache.hGet("wjf","dd"));
    }
}
