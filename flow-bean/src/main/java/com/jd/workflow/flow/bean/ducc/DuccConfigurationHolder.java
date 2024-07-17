package com.jd.workflow.flow.bean.ducc;

import com.jd.laf.config.ConfiguratorManager;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DuccConfigurationHolder {
    Map<String,Holder> resources = new ConcurrentHashMap<>();
    public  ConfiguratorManager getOrNewConfig(String duccUrl){
         if(duccUrl.indexOf("?") != -1){
             duccUrl = duccUrl.substring(0,duccUrl.indexOf("?"));// url后面的配置认为无效
         }

         Holder ret = resources.computeIfAbsent(duccUrl,vs->{
             Holder holder = new Holder();
             holder.setConfiguratorManager(new ConfiguratorManager());
             holder.setRefCount(new AtomicInteger(0));
             return holder;
         });
         ret.getRefCount().addAndGet(1);
         return ret.getConfiguratorManager();
    }
    /*public synchronized ConfiguratorManager get(String key){
        Holder holder = resources.get(key);
        if(holder != null){
            holder.refCount.addAndGet(1);
            return holder.getConfiguratorManager();
        }
        return null;
    }*/

    public synchronized void release(String key){
         Holder holder = resources.get(key);
         int value = holder.getRefCount().decrementAndGet();
         if(value == 0){
             resources.remove(key);
         }
    }
    @Data
    public static class Holder{
        ConfiguratorManager configuratorManager;
        AtomicInteger refCount;
    }
}
