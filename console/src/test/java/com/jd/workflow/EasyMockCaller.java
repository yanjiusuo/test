package com.jd.workflow;

import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.jsf.gd.registry.ClientRegistry;
import com.jd.jsf.gd.registry.RegistryFactory;
import com.jd.jsf.gd.util.Constants;
import com.jd.jsf.gd.util.JSFContext;

import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.y.jsf.HttpMckJsf;
import com.jd.y.model.dto.HttpInterfaceAddDTO;
import com.jd.y.model.vo.HttpInterfaceVO;
import com.jd.y.response.ReplyVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

public class EasyMockCaller {
    static final Logger logger = LoggerFactory.getLogger(EasyMockCaller.class);
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
        ConsumerConfig<HttpMckJsf> consumerConfig = new ConsumerConfig<HttpMckJsf>();
        consumerConfig.setInterfaceId("com.jd.y.jsf.HttpMckJsf");
        consumerConfig.setAlias("easymock-local");
        consumerConfig.setRegistry(registryConfig);
        consumerConfig.setProtocol("jsf");

        consumerConfig.setTimeout(10000);
        String host = "10.0.188.11";
        String callUrl = "jsf://"+host+":22000?alias=easymock-local";
        consumerConfig.setUrl(callUrl);
        //consumerConfig.setParameter(".token","123456");
        HttpMckJsf mckJsf = consumerConfig.refer();
        //相同的接口+别名只需要调用一次订阅即可，不需要周期性调用，通过创建ProviderListener实现，增量的添加（addProvider），删除（removeProvider），和全量的定期更新（updateProvider）；
        HttpInterfaceAddDTO httpDto = new HttpInterfaceAddDTO();
        httpDto.setCreatedBy("wangjingfang3");
        httpDto.setCreatedTme(new Date());
        httpDto.setMethod(httpDto.getMethod());
        httpDto.setPlatform(2);
        httpDto.setMethod("GET");
        httpDto.setName("jfTest");
        httpDto.setMockSwitch(1);
        httpDto.setUrl("/remove/http121");

        final ReplyVO<HttpInterfaceVO> replyVo = mckJsf.addHttpInterface(httpDto);
        System.out.println("response::::"+JsonUtils.toJSONString(replyVo));
    }
}
