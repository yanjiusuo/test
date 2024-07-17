package com.jd.workflow.metrics.client;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GrafanaClient {

    static final String BASE_URL = "http://localhost:3000";
    static String Authorization = null;
    static{
        Authorization = "Basic "+StringHelper.encodeBase64("admin:admin".getBytes(StringHelper.CHARSET_UTF8));
    }

    RequestClient requestClient = null;
    public GrafanaClient(){
        Map<String,Object> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Authorization",Authorization);
        requestClient = new RequestClient(BASE_URL,defaultHeaders);
    }


    public String delete(String uri){
       return requestClient.delete(uri);
    }
    public String post(String path,Map<String,Object> params, Object request){
        return requestClient.post(path, params, request);
    }
    public String delete(String path,Map<String,Object> params){
        return requestClient.delete(path,params);
    }
    public String put(String path,Map<String,Object> params, Object request){
        return requestClient.put(path, params, request);
    }
    public String get(String path, Map<String,Object> params){
        try{
            return requestClient.get(path,params);
        }catch (StdException e){
            return null;
        }

    }

    public static void main(String[] args) throws MalformedURLException, URISyntaxException, UnsupportedEncodingException {

        String result = StringHelper.encodeBase64("admin:admin".getBytes("utf-8"));
        System.out.println(result);
    }
}
