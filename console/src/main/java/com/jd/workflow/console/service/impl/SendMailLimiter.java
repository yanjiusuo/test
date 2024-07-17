package com.jd.workflow.console.service.impl;

import com.jd.workflow.soap.common.cache.ICache;
import com.jd.workflow.soap.common.cache.impl.MemoryCache;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SendMailLimiter implements InitializingBean {
    static final String  CACHE_KEY = "sendMail";
    @Value("${mail.send_interval:300}")
    private int sendInterval=300;
    ICache cache;

    @Override
    public void afterPropertiesSet() throws Exception {
        cache = new MemoryCache();
    }

    public boolean canSend(String key){
        Object value = cache.hGet(CACHE_KEY, key);
        if(value == null){
            cache.hSet(CACHE_KEY,key,true,sendInterval);
            return true;
        }
        return false;
    }
}
