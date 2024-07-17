package com.jd.workflow.flow.bean.ducc;

import com.jd.laf.config.ConfiguratorManager;
import com.jd.laf.config.Property;
import com.jd.laf.config.Resource;
import com.jd.workflow.flow.core.bean.annotation.FlowConfigParam;
import com.jd.workflow.flow.core.bean.annotation.FlowMethod;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.StringHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DuccFlowBean {
    static DuccConfigurationHolder duccConfigurationHolder = new DuccConfigurationHolder();
    //static String URI_TEMPLATE = "ucc://{appName}:{token}@{domain}/v1/namespace/{namespace}/config/{config}/profiles/{profile}?longPolling=60000&necessary=false" ;
    static String APP_NAME = "flow_manage";

    ConfiguratorManager configuratorManager;
    public DuccFlowBean(DuccConfig config){

        ConfiguratorManager configuratorManager = duccConfigurationHolder.getOrNewConfig(config.getUrl());
        configuratorManager.addResource(new Resource("ucc",config.getUrl()));
        configuratorManager.setApplication(APP_NAME);
        this.configuratorManager = configuratorManager;

    }
    public void init(){
        try {
            configuratorManager.start();
        } catch (Exception e) {
            throw StdException.adapt(e);
        }
    }
    public void destroy(){
        configuratorManager.stop();
    }

    @FlowMethod(desc = "获取key对应的ducc属性配置")
    public String getProperty(@FlowConfigParam(desc="ducc配置key") String key){
        Property property = configuratorManager.getProperty(key);
        return property == null ? null : property.getString();
    }
    @FlowMethod(desc="获取key对应的ducc属性配置，若为空，则返回defaultValue")
    public String getPropertyWithDefault(@FlowConfigParam(desc="ducc配置key")String key,@FlowConfigParam(desc="默认值")String defaultValue){
        Property property = configuratorManager.getProperty(key);
        return property == null ? null : property.getString(defaultValue);
    }


    @FlowMethod(desc="获取ducc下配置属性列表")
    public Map<String,String> getProperties(){
        List<Property> propertyList = configuratorManager.getProperties();
        if(propertyList == null){
            return new LinkedHashMap<>();
        }
        Map<String,String> properties = new HashMap<>();
        for (Property property : propertyList) {
            properties.put(property.getName(),property.getString());
        }
        return properties;
    }
}
