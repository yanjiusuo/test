package com.jd.workflow.jsf.example;

import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.registry.ClientRegistry;
import com.jd.jsf.gd.registry.Provider;
import com.jd.jsf.gd.registry.ProviderListener;
import com.jd.jsf.gd.registry.RegistryFactory;
import com.jd.jsf.gd.util.Constants;
import com.jd.jsf.gd.util.JSFContext;
import com.jd.workflow.jsf.service.test.ComplexTypeClass;
import com.jd.workflow.jsf.service.test.IPersonService;
import com.jd.workflow.jsf.service.test.Person;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsfCaller {
    static final Logger logger = LoggerFactory.getLogger(JsfCaller.class);
    //全局只需要一个注册中心client
    private static ClientRegistry CLIENT_REGISTRY;
    private static RegistryConfig registryConfig;
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
        //设置provider列表可为null，用于防止jsf1.6.X及以上版本null保护
        JSFContext.putGlobalVal(Constants.SETTING_CONSUMER_PROVIDER_NULLABLE, "true");

        //注册中心配置信息，通过index寻址注册中心地址；
        List<RegistryConfig> registryConfigs = RegistryFactory.getRegistryConfigs();
        if(registryConfigs!=null && !registryConfigs.isEmpty()) {
            registryConfig = registryConfigs.get(0);
        } else {
            registryConfig = new RegistryConfig();
            registryConfig.setIndex("i.jsf.jd.com");
        }
        //仅订阅注册中心服务发现
        CLIENT_REGISTRY = RegistryFactory.getRegistry(registryConfig);
    }

    public final static void main(String[] args) throws InterruptedException {
        //配置订阅服务的信息，主要配置接口名+别名
        ConsumerConfig<IPersonService> consumerConfig = new ConsumerConfig<IPersonService>();
        consumerConfig.setInterfaceId("com.jd.workflow.jsf.service.test.IPersonService");
        consumerConfig.setAlias("local-debug-test");
        consumerConfig.setRegistry(registryConfig);
        consumerConfig.setProtocol("jsf");

        consumerConfig.setTimeout(10000);
        String host = JSFContext.getLocalHost();
        //String callUrl = "jsf://"+host+":22000?alias=center";
        //consumerConfig.setUrl(callUrl);
        consumerConfig.setParameter(".token","123456");
        IPersonService personService = consumerConfig.refer();
        //相同的接口+别名只需要调用一次订阅即可，不需要周期性调用，通过创建ProviderListener实现，增量的添加（addProvider），删除（removeProvider），和全量的定期更新（updateProvider）；
       /* List<Provider> curProviderList = CLIENT_REGISTRY.subscribe(consumerConfig, new ProviderListener() {
            //增量添加provider，比如上线，动态别名，注册等
            @Override
            public void addProvider(List<Provider> providers) {
                System.out.println("addProvider:" + JsonUtils.toJSONString(providers));
            }
            //增量删除provider，比如下线，反注册，取消动态别名等
            @Override
            public void removeProvider(List<Provider> providers) {
                System.out.println("removeProvider:" + JsonUtils.toJSONString(providers));
            }
            //全量更新 provider 列表，一般是 jsf sdk 定期拉取全量 provider 时触发，周期为上面提到的120秒；
            //CLIENT_REGISTRY 内部会维护一个版本号，如果版本号出现落后的情况下会回调此方法，此方法是为了防止注册中心推送失败
            @Override
            public void updateProvider(List<Provider> providers) {
                System.out.println("updateProvider:" + JsonUtils.toJSONString(providers));
            }
        },null);
        System.out.println("curProviderList:" + JsonUtils.toJSONString(curProviderList));*/
        /*Person p = new Person();
        Object target = personService.simpleDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        System.out.println("================");
        System.out.println(JsonUtils.toJSONString(target));

        Map<String, Object> map = personService.mapType(new HashMap<>());
        logger.info(JsonUtils.toJSONString(map));*/
        final ComplexTypeClass complexTypeClass = new ComplexTypeClass();
       // complexTypeClass.setQNameVar(new QName("xx","fdssd"));
        final ComplexTypeClass result = personService.test(complexTypeClass);

        Person ret = personService.save(new Person());
        System.out.println("========person::"+ret);
        /*synchronized (JsfCaller.class) {
            JsfCaller.class.wait();
        }*/
    }
}
