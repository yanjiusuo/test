package com.jd.workflow.server.controller;

import com.jd.jsf.gd.GenericService;
import com.jd.jsf.gd.config.ConsumerConfig;
import com.jd.jsf.gd.config.RegistryConfig;
import com.jd.mlaas.titan.profiler.sdk.TitanProfilerContext;
import com.jd.mlaas.titan.profiler.sdk.metric.*;
import com.jd.mlaas.titan.profiler.sdk.metric.impl.TitanStaticMetricLabelSuppliers;
import com.jd.workflow.jsf.enums.JsfRegistryEnvEnum;
import com.jd.workflow.soap.common.exception.BizException;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import io.prometheus.client.Summary;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

@Controller
@Slf4j
@RequestMapping("/alarm")
public class AlarmCallbackController {
    HttpClient httpClient = null;
    @Value("${http.maxHttpThreadCount:500}")
    int maxHttpThreadCount = 500;
    @Value("${http.maxHttpThreadPerRoute:200}")
    int maxHttpThreadPerRoute = 200;
    /*
    {"headers":{},"body":{"receiver":"webhook-echo","status":"firing","alerts":[{"status":"firing","labels":{"alertname":"requests_latency_mill_seconds_callCounter","metric":"requests_latency_mill_seconds"},"annotations":{"runbook_url":"http://localhost:8020/alarm/callback","summary":"[在线联调]aaa出现异常 指标名:调用量,配置值：3,实际:104.75819164985674 "},"startsAt":"2022-08-10T17:45:06.915129+08:00","endsAt":"0001-01-01T00:00:00Z","generatorURL":"http://localhost:3000/alerting/grafana/requests_latency_mill_seconds_callCounter/view","fingerprint":"73aa5d3591d912cc","silenceURL":"http://localhost:3000/alerting/silence/new?alertmanager=grafana&matcher=alertname%3Drequests_latency_mill_seconds_callCounter&matcher=metric%3Drequests_latency_mill_seconds","dashboardURL":"","panelURL":"","valueString":"[ var='condition0' metric='sum(increase(requests_latency_mill_seconds_count[1m]))' labels={} value=104.75819164985674 ]"}],"groupLabels":{},"commonLabels":{"alertname":"requests_latency_mill_seconds_callCounter","metric":"requests_latency_mill_seconds"},"commonAnnotations":{"runbook_url":"http://localhost:8020/alarm/callback","summary":"[在线联调]aaa出现异常 指标名:调用量,配置值：3,实际:104.75819164985674 "},"externalURL":"http://localhost:3000/","version":"1","groupKey":"{}/{}:{}","truncatedAlerts":0,"orgId":1,"title":"[FIRING:1]  (requests_latency_mill_seconds_callCounter requests_latency_mill_seconds)","state":"alerting","message":"**Firing**\n\nValue: [ var='condition0' metric='sum(increase(requests_latency_mill_seconds_count[1m]))' labels={} value=104.75819164985674 ]\nLabels:\n - alertname = requests_latency_mill_seconds_callCounter\n - metric = requests_latency_mill_seconds\nAnnotations:\n - runbook_url = http://localhost:8020/alarm/callback\n - summary = [在线联调]aaa出现异常 指标名:调用量,配置值：3,实际:104.75819164985674 \nSource: http://localhost:3000/alerting/grafana/requests_latency_mill_seconds_callCounter/view\nSilence: http://localhost:3000/alerting/silence/new?alertmanager=grafana&matcher=alertname%3Drequests_latency_mill_seconds_callCounter&matcher=metric%3Drequests_latency_mill_seconds\n"},"params":null}
     */
    @RequestMapping(value = "/callback",method = RequestMethod.POST)
    @ResponseBody
    public String add(HttpServletRequest request) throws Exception {
        log.info("alarm.receive_request_data:body={}", IOUtils.toString(request.getInputStream(),"utf-8"));
        return "ok`";
    }
    @PostConstruct
    public void init(){
        httpClient = newClient();
        newConsumer();
    }
    private HttpClient newClient(){
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(5000)
                .setSocketTimeout(5000)
                .build();


        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(500);
        connectionManager.setDefaultMaxPerRoute(200);//例如默认每路由最高50并发，具体依据业务来定


        HttpClientBuilder builder =HttpClientBuilder.create().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig);
        return builder.build();
    }

    @RequestMapping(value = "/originHttpTest")
    @ResponseBody
    public String testExecuteOriginal(){
        String url = "http://http-auth-test.jd.com/userCopy/getUserById";
        String data = "{\"id\":1,\"name\":\"wjf\"}";


        long start = System.currentTimeMillis();


        HttpGet post = new HttpGet();
        try {
            post.setURI(new URI(url));
            HttpResponse response = httpClient.execute(post);

            return EntityUtils.toString(response.getEntity());
        }catch (Exception e) {
            e.printStackTrace();
            return "error";
        }

    }
    GenericService genericService;
    private void newConsumer(){
        String interfaceName = "com.jd.workflow.soap.example.jsf.entity.IUserService";
        ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>();
        consumerConfig.setInterfaceId(interfaceName);// 这里写真实的类名
      /*  RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setAddress("i.jsf.jd.com");
        consumerConfig.setRegistry(jsfRegistry);*/
        RegistryConfig jsfRegistry = new RegistryConfig();
        jsfRegistry.setIndex(JsfRegistryEnvEnum.online.getAddress());
        consumerConfig.setProtocol("jsf");
        consumerConfig.setTimeout(5000);
        consumerConfig.setRegistry(jsfRegistry);
        consumerConfig.setAlias("center");
        consumerConfig.setParameter(".warning","true");
        consumerConfig.setGeneric(true);
        if(consumerConfig.getParameters() == null){
            consumerConfig.setParameters(new HashMap<>());
        }
        consumerConfig.getParameters().put(".token","123456");
        this.genericService = consumerConfig.refer();
    }
    @RequestMapping(value = "/genericJsfCall")
    @ResponseBody
    public String genericJsfCall(){

        Object result = genericService.$invoke("getUser", new String[]{"java.lang.Long"},
                new Object[]{309L});
        return result != null ? result.toString() : "null";
    }

}
