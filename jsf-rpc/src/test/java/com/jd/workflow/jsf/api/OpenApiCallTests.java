package com.jd.workflow.jsf.api;


import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.ProviderConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.registry.ClientRegistry;
import com.jd.jsf.gd.registry.Provider;
import com.jd.jsf.gd.registry.RegistryFactory;
import com.jd.jsf.gd.util.Constants;
import com.jd.jsf.gd.util.JSFContext;
import com.jd.jsf.gd.util.NetUtils;
import com.jd.jsf.open.api.InstanceService;
import com.jd.jsf.open.api.InterfaceService;
import com.jd.jsf.open.api.ProviderAliaService;
import com.jd.jsf.open.api.ProviderService;
import com.jd.jsf.open.api.domain.Server;
import com.jd.jsf.open.api.vo.InterfaceInfo;
import com.jd.jsf.open.api.vo.Result;
import com.jd.jsf.open.api.vo.request.InstanceRequest;
import com.jd.jsf.open.api.vo.request.QueryInterfaceRequest;
import com.jd.jsf.open.api.vo.request.QueryMethodInfoRequest;
import com.jd.jsf.open.api.vo.request.QueryProviderRequest;
import com.jd.workflow.BaseTestCase;

import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

public class OpenApiCallTests extends BaseTestCase {
    //全局只需要一个注册中心client
    private static ClientRegistry CLIENT_REGISTRY;
    private static RegistryConfig registryConfig;
    @BeforeClass
    public static void beforeClass(){
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
        //CLIENT_REGISTRY = RegistryFactory.getRegistry(registryConfig);
    }
    @Test
    public void testQueryAppName() throws Exception {
        //配置订阅服务的信息，主要配置接口名+别名
        ConsumerConfig<ProviderService> consumerConfig = new ConsumerConfig<>();
        consumerConfig.setInterfaceId(ProviderService.class.getName());
        consumerConfig.setAlias("jsf-open-api");
        //consumerConfig.setRegistry(registryConfig);
        //consumerConfig.setProtocol("jsf");

        consumerConfig.setTimeout(10000);
        String host = JSFContext.getLocalHost();
        //String callUrl = "jsf://"+host+":22000?alias=center";
        //consumerConfig.setUrl(callUrl);
        ProviderService providerService = consumerConfig.refer();
        QueryProviderRequest req = new QueryProviderRequest();
        String appKey = "jdos_data-flow" ;//Your appName in Jsf admin
        String token = "abc31"; //Your appName  token 如果线上环境可以在管理端app管理页面中点击修改按钮进行手动添加
        String operator = "wangjingfang3";
        req.setAppKey(appKey);
        req.setOperator(operator);
        req.setTimeStamp(System.currentTimeMillis());
        req.setSign(req.sign(token ));
        req.setClientIp(NetUtils.getLocalHost());
        req.setInterfaceName("com.jd.workflow.server.service.InterfaceGetRpcService");
        Result<List<Server>> result = providerService.query(req);
        List<Server> data = result.getData();
        if(result.getData() != null && !result.getData().isEmpty()){
            Server server = result.getData().get(0);
            String attrUrl = server.getAttrUrl();
            if(StringUtils.isNotBlank(attrUrl)){
                attrUrl = attrUrl.replace("{","").replace("}","");
                List<String> list = StringHelper.split(attrUrl, ",");
                for (String s : list) {
                    List<String> nameValue = StringHelper.split(s, "=");
                    if(nameValue.size() == 2){
                        if("appName".equals(nameValue.get(0).trim())){
                            String appName = nameValue.get(1).trim();
                            String jdosAppCode = appName.substring("jdos_".length());
                            if(appName != null && appName.startsWith("jdos_")){
                                System.out.println(appName);
                            }
                            break;
                        }
                    }
                }
            }


        }
        System.out.println("--------------------------------");
        System.out.println(JsonUtils.toJSONString(result));

        QueryMethodInfoRequest methodReq = newMethodReq();
        methodReq.setInterfaceName("com.jd.mpaas.console.jsf.service.MpaasRemoteService");
        methodReq.setMethodName("createApp");






        synchronized (OpenApiCallTests.class) {
            OpenApiCallTests.class.wait();
        }
    }
    @Test
    public void testGetInterface() throws Exception {
        //配置订阅服务的信息，主要配置接口名+别名
        ConsumerConfig<InterfaceService> consumerConfig = new ConsumerConfig<InterfaceService>();
        consumerConfig.setInterfaceId("com.jd.jsf.open.api.InterfaceService");
        consumerConfig.setAlias("jsf-open-api");
        //consumerConfig.setRegistry(registryConfig);
        //consumerConfig.setProtocol("jsf");

        consumerConfig.setTimeout(10000);
        String host = JSFContext.getLocalHost();
        //String callUrl = "jsf://"+host+":22000?alias=center";
        //consumerConfig.setUrl(callUrl);
        InterfaceService interfaceService = consumerConfig.refer();
        QueryInterfaceRequest req = newReq();

        req.setInterfaceName("com.jd.mpaas.console.jsf.service.MpaasRemoteService");
        Result<InterfaceInfo> result = interfaceService.getByInterfaceName(req);
        InterfaceInfo data = result.getData();
        System.out.println("--------------------------------");
        System.out.println(JsonUtils.toJSONString(result));

        QueryMethodInfoRequest methodReq = newMethodReq();
        methodReq.setInterfaceName("com.jd.mpaas.console.jsf.service.MpaasRemoteService");
        methodReq.setMethodName("createApp");




        Result<String> methodInfo = interfaceService.getMethodInfo(methodReq);
        System.out.println("------------methodInfo--------------------");
        System.out.println(JsonUtils.toJSONString(methodInfo));

        synchronized (OpenApiCallTests.class) {
            OpenApiCallTests.class.wait();
        }
    }
    @Test
    public void testQueryAlias() throws Exception {
        //配置订阅服务的信息，主要配置接口名+别名
        ConsumerConfig<ProviderAliaService> consumerConfig = new ConsumerConfig<ProviderAliaService>();
        consumerConfig.setInterfaceId("com.jd.jsf.open.api.ProviderAliaService");
        consumerConfig.setAlias("jsf-open-api");
        //consumerConfig.setRegistry(registryConfig);
        //consumerConfig.setProtocol("jsf");

//        ProviderConfig providerConfig = null;
//        providerConfig.export();

        consumerConfig.setTimeout(10000);
        String host = JSFContext.getLocalHost();
        //String callUrl = "jsf://"+host+":22000?alias=center";
        //consumerConfig.setUrl(callUrl);
        ProviderAliaService aliaService = consumerConfig.refer();
        QueryInterfaceRequest req = newReq();

        req.setInterfaceName("com.jd.mpaas.console.jsf.service.MpaasRemoteService");

        Result<List<String>> result = aliaService.getAliasByInterfaceName(req);
        Provider provider = null;

        System.out.println("--------------------------------");
        System.out.println(JsonUtils.toJSONString(result));



        synchronized (OpenApiCallTests.class) {
            OpenApiCallTests.class.wait();
        }
    }

    /**
     * 查询服务提供者
     * @throws InterruptedException
     */
    @Test
    public void testQueryProviders() throws InterruptedException {
        //配置订阅服务的信息，主要配置接口名+别名
        ConsumerConfig<InterfaceService> consumerConfig = new ConsumerConfig<InterfaceService>();
        consumerConfig.setInterfaceId("com.jd.jsf.open.api.InstanceService");
        consumerConfig.setAlias("jsf-open-api");
        //consumerConfig.setRegistry(registryConfig);
        //consumerConfig.setProtocol("jsf");

        consumerConfig.setTimeout(10000);
        String host = JSFContext.getLocalHost();
        //String callUrl = "jsf://"+host+":22000?alias=center";
        //consumerConfig.setUrl(callUrl);
        InstanceService interfaceService = (InstanceService) consumerConfig.refer();
        InstanceRequest req = new InstanceRequest();
       // req.setInsIp(NetUtils.getLocalHost());
        req.setInsIp("11.51.194.204");
        req.setAppKey("jdos_data-flow");
        req.setOperator("wangjingfang3");
        String token = "abc31";
        req.setTimeStamp(System.currentTimeMillis());
        req.setSign(req.sign(token ));
        req.setClientIp(NetUtils.getLocalHost());
        req.setAppKey("jdos_data-flow");
        Result<List<Server>> listResult = interfaceService.queryProviders(req);

        System.out.println("--------------------------------");
        System.out.println(JsonUtils.toJSONString(listResult));


        System.out.println("------------methodInfo--------------------");


        synchronized (OpenApiCallTests.class) {
            OpenApiCallTests.class.wait();
        }
    }
    QueryMethodInfoRequest newMethodReq(){
        QueryMethodInfoRequest req = new QueryMethodInfoRequest();
        String appKey = "jdos_data-flow" ;//Your appName in Jsf admin
        String token = "abc31"; //Your appName  token 如果线上环境可以在管理端app管理页面中点击修改按钮进行手动添加
        String operator = "wangjingfang3";
        req.setAppKey(appKey);
        req.setOperator(operator);
        req.setTimeStamp(System.currentTimeMillis());
        req.setSign(req.sign(token ));
        req.setClientIp(NetUtils.getLocalHost());

        return req;
    }
    QueryInterfaceRequest newReq(){
        QueryInterfaceRequest req = new QueryInterfaceRequest();
        String appKey = "jdos_data-flow" ;//Your appName in Jsf admin
        String token = "abc31"; //Your appName  token 如果线上环境可以在管理端app管理页面中点击修改按钮进行手动添加
        String operator = "wangjingfang3";
        req.setAppKey(appKey);
        req.setOperator(operator);
        req.setTimeStamp(System.currentTimeMillis());
        req.setSign(req.sign(token ));
        req.setClientIp(NetUtils.getLocalHost());

        return req;
    }
}
