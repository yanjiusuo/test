package com.jd.workflow.metrics.client;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;


import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public class TitanRequestClient {
    String source;
    String token;
    static final String BASE_URL = "http://11.50.162.244";
    RequestClient requestClient = null;
    public TitanRequestClient(String token){
        Map<String,Object> defaultHeaders = new HashMap<>();
        defaultHeaders.put("Authorization","Bearer "+token);
        requestClient = new RequestClient(BASE_URL,defaultHeaders);

        this.token = token;

    }
    public String delete(String uri){
        return requestClient.delete(uri);
    }

    public String post(String uri, Object request){
        return requestClient.post(uri,null,request);
    }
    public String put(String uri, Object request){
        return requestClient.put(uri,null,request);
    }
    public String get(String path, Map<String,Object> params){
        return requestClient.get(path,params);
    }

}
