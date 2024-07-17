package com.jd.workflow.jsf.generic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 项目名称：parent
 * 类 名 称：JsfClientFactory
 * 类 描 述：TODO
 * 创建时间：2022-06-28 11:30
 * 创 建 人：wangxiaofei8
 */
@Data
@Slf4j
public class JsfClientFactory {

    private static final Object LOCK = new Object();

    private static ConcurrentMap<String, JsfClientProxy> jsfClientProxyMap = new ConcurrentHashMap<>();

    public static JsfClientProxy getProxy(JsfApi jsfApi) {
        JsfClientProxy client = jsfClientProxyMap.get(jsfApi.getConsumerName());
        if (null == client) {
            client = addJsfConsumer(makeConfig(jsfApi));
        }else if(BooleanUtils.isTrue(jsfApi.getUpdateInstance())){
            removeJsfConsumer(jsfApi.getConsumerName());
            client = addJsfConsumer(makeConfig(jsfApi));
        }
        return client;
    }

    private static JsfClientProxy addJsfConsumer(JsfConsumerConfig config) {
        String invokerName = config.getConsumerName();
        JsfClientProxy proxy = jsfClientProxyMap.get(invokerName);
        if(null == proxy) {
            synchronized (LOCK) {
                proxy = jsfClientProxyMap.get(invokerName);
                if(null == proxy) {
                    proxy =  buildJsfClient(config);
                    jsfClientProxyMap.putIfAbsent(invokerName, proxy);
                    log.info("Create JsfConsumer Object:" + config);
                }
            }
        }
        return proxy;
    }

    private static void removeJsfConsumer(String consumerName) {
        JsfClientProxy proxy = jsfClientProxyMap.get(consumerName);
        if(null != proxy) {
            synchronized (LOCK) {
                proxy = jsfClientProxyMap.get(consumerName);
                if(null != proxy) {
                    proxy.destory();
                    jsfClientProxyMap.remove(consumerName);
                    log.info("Remove JsfConsumer Object , consumerName:" + consumerName);
                }
            }
        }
    }


    private static JsfConsumerConfig makeConfig(JsfApi jsfApi) {
        JsfConsumerConfig config = new JsfConsumerConfig();
        config.setInterfaceClassName(jsfApi.getJsfInterface());
        config.setAlias(jsfApi.getAlias());
        config.setProtocol(jsfApi.getProtocol());
        config.setSerialization(jsfApi.getSerialization());
        config.setTimeout(jsfApi.getTimeout());
        config.setIndex(jsfApi.getIndex());
        config.setGeneric(jsfApi.getGeneric());
        if (StringUtils.isNotBlank(jsfApi.getConfigParams())) {
            List<JsfParameterConfig> configs = JsonUtils.parse(jsfApi.getConfigParams(),
                    new TypeReference<List<JsfParameterConfig>>() {});
            config.getJsfParameterConfigs().addAll(configs);
        }
        return config;
    }

    private static JsfClientProxy buildJsfClient(JsfConsumerConfig config) {
        return new JsfClientProxy(config);
    }

}
