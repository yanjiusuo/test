package com.jd.workflow.jsf.generic;

import com.jd.jsf.gd.GenericService;
import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;

/**
 * 项目名称：parent
 * 类 名 称：JsfClientProxy
 * 类 描 述：TODO
 * 创建时间：2022-06-28 11:10
 * 创 建 人：wangxiaofei8
 */
public class JsfClientProxy {

    private ConsumerConfig<?> consumerConfig;
    private GenericService genericService;

    public JsfClientProxy(JsfConsumerConfig config) {
        ConsumerConfig<?> cfg = new ConsumerConfig<>();
        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex(config.getIndex());
        cfg.setRegistry(jsfRegistry);
        cfg.setInterfaceId(config.getInterfaceClassName());
        cfg.setSerialization(config.getSerialization());
        cfg.setProtocol(config.getProtocol());
        cfg.setAlias(config.getAlias());
        cfg.setTimeout(config.getTimeout());
        cfg.setParameters(config.toJsfParameterConfigMap(cfg.getParameters()));
        cfg.setGeneric(config.isGeneric());
        this.consumerConfig = cfg;
        this.genericService = (GenericService)this.consumerConfig.refer();
    }

    public Object invoke(GenericRequest request) {
        return genericService.$invoke(request.getMethod(), request.getParameterTypes(), request.getArgs());
    }

    /**
     * 取消客户端调用配置
     */
    public void destory() {
        consumerConfig.unrefer();
        genericService = null;
        consumerConfig = null;
    }
}
