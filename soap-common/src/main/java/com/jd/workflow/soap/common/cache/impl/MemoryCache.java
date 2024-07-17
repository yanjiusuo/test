package com.jd.workflow.soap.common.cache.impl;

import com.jd.workflow.soap.common.cache.AbstractCache;
import com.jd.workflow.soap.common.cache.ICacheExpireListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class MemoryCache extends AbstractCache {
    List<ICacheExpireListener> listeners = new CopyOnWriteArrayList<>();
    Map<String, Object> cached = new ConcurrentHashMap<>();
    long defaultCheckInterval = 10 * 1000;
    boolean isChecking = false;
    long lastCheckTime;

    void checkExpire(long cleanTime) {
        if (cleanTime >= 0L) {
            long currentTime = System.currentTimeMillis();
            if (this.lastCheckTime + cleanTime < currentTime) {
                this.doCheckExpire();
            }
        }
    }
    public void addListener(ICacheExpireListener listener){
        listeners.add(listener);
    }

    private void doCheckExpire() {
        synchronized (cached) {
            if (this.isChecking) {
                return;
            }

            this.isChecking = true;
        }

        this.lastCheckTime = System.currentTimeMillis();

        Iterator iterator = cached.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            CachedObject cachedObject = (CachedObject) entry.getValue();

            if (cachedObject.isExpire() ) {
                cached.remove(entry.getKey(), cachedObject);
                /*for (ICacheExpireListener listener : listeners) {
                    listener.onExpire(cachedObject.getCachedObject());
                }*/
                callListener(cachedObject.getCachedObject());
                if (log.isDebugEnabled()) {
                    log.debug("cache.remove_expired_item:key={}, value={}", entry.getKey(), entry.getValue());
                }

            }
        }

        synchronized (cached) {
            this.isChecking = false;
        }


    }

    public boolean isEmpty(){
        return cached.isEmpty();
    }

    @Override
    public void hSet(String key, String field, Object obj, int seconds) {
        this.checkExpire(seconds);
        CachedObject cachedObject = new CachedObject();
        cachedObject.setCachedObject(obj);
        cachedObject.setStartTime(System.currentTimeMillis());
        cachedObject.setCachedTimeInSeconds(seconds);
        cached.put(cachedKey(key, field), cachedObject);
    }

    @Override
    public <T> T hGet(String key, String field) {
        this.checkExpire(defaultCheckInterval);
        String cachedKey = cachedKey(key, field);
        CachedObject cachedObject = (CachedObject) cached.get(cachedKey);
        if (cachedObject != null) {
            if (cachedObject.isExpire()) {
                /*for (ICacheExpireListener listener : listeners) {
                    listener.onExpire(cachedObject.getCachedObject());
                }*/
                callListener(cachedObject.getCachedObject());
                cached.remove(cachedKey);
                return null;
            }
            cachedObject.setStartTime(System.currentTimeMillis());
            return (T) cachedObject.getCachedObject();
        }
        return null;
    }

    @Override
    public <T> T hRemove(String key, String field) {
        this.checkExpire(defaultCheckInterval);
        String cachedKey = cachedKey(key, field);
        CachedObject remove = (CachedObject) cached.remove(cachedKey);
        if (remove != null) {
            /*for (ICacheExpireListener listener : listeners) {
                listener.onExpire(remove.getCachedObject());
            }*/
            callListener(remove.getCachedObject());
            return (T) remove.getCachedObject();
        }
        return null;
    }
    private void callListener(Object obj){
        for (ICacheExpireListener listener : listeners) {
            try{
                listener.onExpire(obj);
            }catch (Exception e){
                log.error("cache.call_expire_listener_error",e);
            }
        }
    }

    @Data
    private static class CachedObject {
        int cachedTimeInSeconds;
        long startTime;
        Object cachedObject;

        public boolean isExpire() {
            return System.currentTimeMillis() >= startTime + cachedTimeInSeconds * 1000;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        MemoryCache cache = new MemoryCache();
        cache.addListener(new ICacheExpireListener() {
            @Override
            public void onExpire(Object obj) {
                System.out.println(obj+" expired");
            }
        });
        cache.hSet("dd","ee",1,2);
        Thread.sleep(2500);
        Object result = cache.hGet("dd", "ee");
        System.out.println(result);
    }
}
