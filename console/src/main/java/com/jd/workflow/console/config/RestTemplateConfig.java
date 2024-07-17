package com.jd.workflow.console.config;

import com.jd.workflow.console.utils.RestTemplateUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/2 
 */
@Configuration
public class RestTemplateConfig {

    /**
     * 读取
     */
    private int readTimeout = 30000;

    /**
     * 链接
     */
    private int connectTimeout = 5000;

    @Bean
    public ClientHttpRequestFactory simpleFactory(){
        SimpleClientHttpRequestFactory simpleFactory = new SimpleClientHttpRequestFactory();
        simpleFactory.setReadTimeout(readTimeout);
        simpleFactory.setConnectTimeout(connectTimeout);
        return simpleFactory;
    }

    @Bean
    public RestTemplate restTemplate(ClientHttpRequestFactory clientHttpRequestFactory) {
        return new RestTemplate(clientHttpRequestFactory);
    }

    @Bean
    public HttpHeaders httpHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    @Bean
    public RestTemplateUtils restTemplateUtils(RestTemplate restTemplate,HttpHeaders httpHeaders) {
        RestTemplateUtils restTemplateUtils = new RestTemplateUtils();
        restTemplateUtils.setRestTemplate(restTemplate);
        restTemplateUtils.setHeaders(httpHeaders);
        return restTemplateUtils;
    }
}
