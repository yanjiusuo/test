package com.jd.workflow.console.listener.cjg.processor;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.jd.workflow.console.service.IAppInfoService;
import com.jd.workflow.console.service.IUserInfoService;
import com.jd.workflow.soap.common.cache.ICache;
import com.jd.workflow.soap.common.cache.impl.MemoryCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CjgAppConsumerCache implements InitializingBean {

    static final String CACHE_KEY = "cjgAppConsumer";
    ICache cache;
    @Autowired
    IAppInfoService appInfoService;
    @Autowired
    private ScheduledThreadPoolExecutor defaultScheduledExecutor;


    @Override
    public void afterPropertiesSet() throws Exception {
        cache = new MemoryCache();
        ThreadFactory namedThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("appSyncThread").build();

        //executor.setRejectedExecutionHandler(new DefaultRejectExecuteHandler());

    }
    public void set(String key,Object value){
        cache.hSet(CACHE_KEY,key,value,5);
    }
    public Object get(String key){
       return  cache.hGet(CACHE_KEY,key);
    }
    public void syncCjgAppToLocal(String appName,boolean delete){
        synchronized (this){
            if(get(appName) != null) {
                log.info("app.ignore_duplicated_app_data:appName={}",appName);
                return;
            }
            set(appName,true);
        }

        log.info("app.sync_app_data:appName={}",appName);
        // 藏经阁数据库有延迟，2秒后再去处理
        defaultScheduledExecutor.schedule(new Runnable() {
            @Override
            public void run() {
                try{
                    appInfoService.syncCjgAppToLocal(appName,delete);
                }catch (Exception e){
                    log.error("app.err_sync_cjg_app:appName={}",appName,e);
                }
            }
        },2000, TimeUnit.MILLISECONDS);


    }

    public static void main(String[] args) throws Exception {
        CjgAppConsumerCache cache = new CjgAppConsumerCache();
        cache.afterPropertiesSet();
        for (int i = 0; i < 1000; i++) {
            cache.syncCjgAppToLocal("a",true);
        }
    }

}
