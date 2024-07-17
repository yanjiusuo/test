package com.jd.workflow.flow.bean.jimdb;

import com.jd.jim.cli.Cluster;
import com.jd.jim.cli.config.ConfigLongPollingClientFactory;
import com.jd.jim.cli.protocol.DataType;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import com.jd.jim.cli.ReloadableJimClientFactory;
import com.jd.workflow.flow.core.bean.annotation.FlowMethod;
import com.jd.workflow.soap.common.util.StringHelper;

public class JimdbFlowBean {
    private Cluster jimClient;
    JimdbConfig jimdbConfig;
    public JimdbFlowBean(JimdbConfig jimdbConfig){
        this.jimdbConfig = jimdbConfig;
    }
    private final String jimUrl = "jim://2914179041246144323/110000253";
    private final String serviceEndpoint = "http://cfs.jim.jd.local/";
    protected ReloadableJimClientFactory factory = null;


    public ReloadableJimClientFactory createFactory(String serviceEndpoint){
        ConfigLongPollingClientFactory configClientFactory = new ConfigLongPollingClientFactory(serviceEndpoint);
        factory = new ReloadableJimClientFactory();

        factory.setJimUrl(jimUrl);
        factory.setIoThreadPoolSize(5);
        factory.setComputationThreadPoolSize(5);
        factory.setRequestQueueSize(100000);
        factory.setConfigClient(configClientFactory.create());
        return factory;
    }


    public Cluster createClient(){
        return factory.getClient();
    }
    public void init(){
        String endpoint = StringHelper.isEmpty(jimdbConfig.getServiceEndpoint()) ? serviceEndpoint : jimdbConfig.getServiceEndpoint();
        ReloadableJimClientFactory factoryBean = createFactory(endpoint);
        factoryBean.setJimUrl(jimdbConfig.getUrl());
        jimClient = factoryBean.getClient();
    }
    public void destroy(){
        jimClient.destroy();
    }

    @FlowMethod(desc = "根据key获取值")
    public  String get(String key){
        return jimClient.get(key);
    }
    @FlowMethod(desc = "设置值")
    public  void set(String key,String value){
        jimClient.set(key,value);
    }
    @FlowMethod(desc = "setEx")
    public void setEx(String key, String value, long timeInMills){
          jimClient.setEx(key,value,timeInMills,TimeUnit.MILLISECONDS);
    }
    @FlowMethod(desc = "pSetEx")
    public void pSetEx(String key, String value, long timeInMills){
        jimClient.pSetEx(key,value,timeInMills,TimeUnit.MILLISECONDS);
    }
    @FlowMethod(desc = "strLen")
    public Long strLen(String key){
        return jimClient.strLen(key);
    }
    @FlowMethod(desc = "append")
    public Long append(String key, String value){
        return jimClient.append(key,value);
    }
    @FlowMethod(desc = "incr")
    public Long incr(String key){
        return jimClient.incr(key);
    }
    @FlowMethod(desc = "incrBy")
    public Long incrBy(String key, long i){
        return jimClient.incrBy(key,i);
    }

    @FlowMethod(desc = "decr")
    public Long decr(String key){
        return jimClient.decr(key);
    }
    @FlowMethod(desc = "decrBy")
    public Long decrBy(String key, long i){
        return jimClient.decrBy(key,i);
    }
    @FlowMethod(desc = "exists")
    public Boolean exists(String key){
        return jimClient.exists(key);
    }
    @FlowMethod(desc = "删除key对应的值")
    public Long del(String key){
        return jimClient.del(key);
    }
    @FlowMethod(desc = "type")
    public String type(String key){
        DataType type = jimClient.type(key);
        if(type == null) return null;
        return type.code();
    }
    @FlowMethod(desc = "hSet")
    public Boolean hSet(String key, String field, String value){
        return jimClient.hSet(key,field,value);
    }
    @FlowMethod(desc = "hGet")
    public String hGet(String key, String field){
        return jimClient.hGet(key,field);
    }
    @FlowMethod(desc = "hExists")
    public Boolean hExists(String key, String field){
        return jimClient.hExists(key,field);
    }
}
