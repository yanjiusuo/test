package com.jd.workflow.local;


import com.jd.jsf.gd.config.ProviderConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.config.ServerConfig;
import com.jd.jsf.gd.util.Constants;
import com.jd.jsf.gd.util.JSFContext;
import com.jd.workflow.local.entity.IUserService;
import com.jd.workflow.local.entity.UserServiceJImpl;


public class JsfPublisher {
    //全局只需要一个注册中心client
    //private static ClientRegistry CLIENT_REGISTRY;
    static {
        //参见https://cf.jd.com/pages/viewpage.action?pageId=245185287#JSF%E5%AE%A2%E6%88%B7%E7%AB%AF%E7%94%A8%E6%88%B7%E6%89%8B%E5%86%8C-APPID%E5%8F%8AAPPNAME%E4%BC%A0%E9%80%92
        //一定要设置这两个字段，会进行校验；
        //不要随意填写，防止和别的应用名字冲突，这个两个字段可以通过jdos申请应用获取，即使不部署也可以申请一个空应用，用于获取appId及appName；
        JSFContext.putIfAbsent( JSFContext.KEY_APPID, "123456" );
        JSFContext.putIfAbsent( JSFContext.KEY_APPNAME, "test_app" );
        //设置jsf客户端与注册中心的心跳间隔，默认30秒；
        JSFContext.putGlobalVal(Constants.SETTING_REGISTRY_HEARTBEAT_INTERVAL, "30000");
        //设置jsf客户端从注册中心定期拉取provider列表的时间间隔，一般设置120秒，不要太快，对注册中心有压力；
        JSFContext.putGlobalVal(Constants.SETTING_REGISTRY_CHECK_INTERVAL, "120000");
        //注册中心配置信息，通过index寻址注册中心地址；

        //仅订阅注册中心服务发现
        //CLIENT_REGISTRY = RegistryFactory.getRegistry(registryConfig);
    }

    public final static void main(String[] args) throws InterruptedException {

        //设置要发布服务的ip及端口
        ServerConfig serverConfig = new ServerConfig();
      /*  serverConfig.setHost(JSFContext.getLocalHost()); // 获取本机ip；
        serverConfig.setPort(22000);*///设置提供服务的端口;

        ProviderConfig<IUserService> providerConfig = new ProviderConfig<>();
        providerConfig.setInterfaceId(IUserService.class.getName());
        providerConfig.setAlias("center123");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setIndex("i.jsf.jd.com");
        providerConfig.setRegistry(registryConfig);
        providerConfig.setParameter(".token","123456");

        providerConfig.setServer(serverConfig);
        providerConfig.setRef(new UserServiceJImpl());
        //重要的：发起注册请求
        //CLIENT_REGISTRY.register(providerConfig,null);
        providerConfig.export();
        synchronized (JsfPublisher.class){
            JsfPublisher.class.wait();
        }
    }
}

