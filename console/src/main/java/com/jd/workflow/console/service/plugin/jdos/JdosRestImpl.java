package com.jd.workflow.console.service.plugin.jdos;

import com.alibaba.fastjson.JSON;
import com.jd.workflow.console.utils.RestTemplateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p>
 * jdos 调用实现
 * </p>
 *
 * @author sunchao81
 * @date 2022-09-19
 */
@Slf4j
@Service
public class JdosRestImpl implements JdosRest {

    /**
     *
     */
    @Resource
    private RestTemplateUtils restTemplateUtils;

    /**
     *
     */
    private static String token = "b820c20a-c93d-4660-9d29-a963b67d0ce2";


    /**
     *
     * @param url
     * @param method get post
     */
    @Override
    public String invoke(String url, HttpMethod method, String jsonParams) {
        String userName = "org.lht";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("erp",userName);
            headers.add("tenant","JDDTEST");
            headers.add("token",token);
            if(HttpMethod.GET.equals(method)){
                log.info("## jdos3 ## url:{},header:{}", url, JSON.toJSONString(headers));
                String rest = restTemplateUtils.get(url, headers);
                log.info("## jdos3 ## url:{},header:{},response:{}", url, JSON.toJSONString(headers), rest);
                return rest;
            }else if(HttpMethod.POST.equals(method)) {
                log.info("## jdos3 ## url:{},header:{}", url, JSON.toJSONString(headers));
                String rest = restTemplateUtils.postJson(url,jsonParams, headers);
                log.info("## jdos3 ## url:{},header:{},response:{}", url, JSON.toJSONString(headers), rest);
                return rest;
            }else {
                log.info("不合法参数url {} # method {}",url,method);
                return null;
            }
        } catch (Exception e) {
            log.info("测试异常", e);
            return null;
        }
    }

    @Override
    public String invokeGet(String url) {
        String userName = "org.lht";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("erp",userName);
            headers.add("tenant","JDDTEST");
            headers.add("token",token);
            log.info("## jdos3 ## url:{},header:{}", url, JSON.toJSONString(headers));
            String result = restTemplateUtils.getAccept(url, headers);
            log.info("## jdos3 ## url:{},header:{},response:{}", url, JSON.toJSONString(headers), result);
            if(result.contains("应用不存在")) {
                HttpHeaders headers2 = new HttpHeaders();
                headers2.add("erp",userName);

                headers2.add("token",token);
                headers2.add("tenant", "JDTTEST");
                log.info("## jdos3 ## url:{},header:{}", url, JSON.toJSONString(headers2));
                result = restTemplateUtils.getAccept(url, headers2);
                log.info("## jdos3 ## url:{},header:{},response:{}", url, JSON.toJSONString(headers2), result);
            }
            return result;
        } catch (Exception e) {
            log.info("测试异常", e);
            return null;
        }
    }

    /**
     *
     * @param url
     * @param tenant
     * @return
     */
    @Override
    public String invokeGet(String url,String tenant) {
        String userName = "org.lht";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("erp",userName);
            headers.add("tenant",tenant);
            headers.add("token",token);
            log.info("## jdos3 ## url:{},header:{}", url, JSON.toJSONString(headers));
            String result = restTemplateUtils.getAccept(url, headers);
            log.info("## jdos3 ## url:{},header:{},response:{}", url, JSON.toJSONString(headers), result);
            return result;
        } catch (Exception e) {
            log.info("测试异常", e);
            return null;
        }
    }

    @Override
    public String invokePost(String url,String jsonParams) {
        String userName = "org.lht";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("erp",userName);
            headers.add("tenant","JDDTEST");
            headers.add("token",token);
            log.info("## jdos3 ## url:{},header:{}", url, JSON.toJSONString(headers));
            String result = restTemplateUtils.postJson(url,jsonParams, headers);
            log.info("## jdos3 ## url:{},header:{},response:{}", url, JSON.toJSONString(headers), result);
            return result;
        } catch (Exception e) {
            log.info("测试异常", e);
            return null;
        }
    }


    @Override
    public String invokePost(String url,String jsonParams,String tenant) {
        String userName = "org.lht";
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add("erp",userName);
            headers.add("tenant",tenant);
            headers.add("token",token);
            log.info("## jdos3 ## url:{},header:{}", url, JSON.toJSONString(headers));
            String result = restTemplateUtils.postJson(url,jsonParams, headers);
            log.info("## jdos3 ## url:{},header:{},response:{}", url, JSON.toJSONString(headers), result);
            return result;
        } catch (Exception e) {
            log.info("测试异常", e);
            return null;
        }
    }

}
