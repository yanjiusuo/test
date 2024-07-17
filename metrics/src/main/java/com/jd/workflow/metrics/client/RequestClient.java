package com.jd.workflow.metrics.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Guard;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.util.Collections;
import java.util.Map;
@Slf4j
public class RequestClient {
    String baseUrl;
    Map<String,Object> defaultHeaders;
    CloseableHttpClient client;
    public RequestClient(String baseUrl,Map<String,Object> defaultHeaders){
        this.baseUrl = baseUrl;
        this.defaultHeaders = defaultHeaders;
        client = HttpClientBuilder.create().build();
    }
    public RequestClient(CookieStore cookieStore) {
        client = HttpClientBuilder.create().setDefaultCookieStore(cookieStore).build();
    }
    public RequestClient() {
        client = HttpClientBuilder.create().build();
    }
    public String execute(String method, String path, Map<String,Object> params, Object request){
        return execute(method,path,params,request,null);
    }
    public String execute(String method, String path, Map<String,Object> params, Object request,Map<String,Object> headers){
        //HttpEntityEnclosingRequestBase req ;
        HttpRequestBase req ;
        if("post".equalsIgnoreCase(method)){
            req = new HttpPost();
        }else if("get".equalsIgnoreCase(method)){
            req = new HttpGet();
        }else if("put".equalsIgnoreCase(method)){
            req = new HttpPut();
        }else {
            req = new HttpDelete();
        }
        if(defaultHeaders != null){
            for (Map.Entry<String, Object> entry : defaultHeaders.entrySet()) {
                req.setHeader(entry.getKey(), Variant.valueOf(entry.getValue()).toString());
            }
        }
        if(headers != null){
            for (Map.Entry<String, Object> entry : headers.entrySet()) {
                req.setHeader(entry.getKey(), Variant.valueOf(entry.getValue()).toString());
            }

        }

        try {
            req.addHeader("Content-Type","application/json;charset=utf-8");
            String queryStr = StringHelper.encodeQuery(params,"utf-8");
            StringBuffer sbUri = new StringBuffer();
            if(StringUtils.isNotEmpty(baseUrl)){
                sbUri.append(baseUrl);
            }
            if(StringUtils.isNotEmpty(path)){
                sbUri.append(path);
            }
            if(!StringHelper.isEmpty(queryStr)){
                sbUri.append("?").append(queryStr);
            }
            String uri = sbUri.toString();
            req.setURI(new URI(uri));
            String body = null;
            if(request != null){
                body = JsonUtils.toJSONString(request);
                ((HttpEntityEnclosingRequestBase)req).setEntity(new StringEntity(body,"utf-8"));
            }

            log.info("httpclient.request_uri:url={},method={},body={}",uri,method,body);
            CloseableHttpResponse response = client.execute(req);
            int statusCode = response.getStatusLine().getStatusCode();
            if(!(statusCode > 199 && statusCode < 300)){ // statusCode > 199 && statusCode < 300

                if(response.getEntity() != null){
                    String msg = EntityUtils.toString(response.getEntity(), "utf-8");
                    log.error("httpclient.err_execute_req:code={},msg={}", statusCode,
                            msg);
                    throw new StdException("httpclient.err_execute_req").param("code",statusCode).param("msg",msg);
                }


            }
            if(response.getEntity() != null){
                String result = EntityUtils.toString(response.getEntity(),"utf-8");
                if(result.length() < 500){
                    log.info("httpclient.success_request_uri:url={},method={},code={},body={},response={}",uri,method,statusCode,body,result);
                }else{
                    log.info("httpclient.success_request_uri:url={},method={},code={},body={}",uri,method,statusCode,body);
                }

                return result;
            }else{
                log.info("httpclient.success_request_uri:url={},method={},code={},body={},response={}",uri,method,statusCode,body,"");
                return "";
            }

        } catch (Exception e) {
            throw StdException.adapt(e);
        }
    }
    public String delete(String uri){

        return execute("delete",uri,null,null);
    }
    public String post(String path,Map<String,Object> params,Map<String,Object> headers, Object request){
        return execute("post",path,params,request,headers);
    }
    public String post(String path,Map<String,Object> params, Object request){
        return execute("post",path,params,request);
    }
    public <T> T post(RequestBuilder request, TypeReference<T> valueTypeRef){
        Guard.notEmpty(request.path,"path不可为空");
        String result = execute("post",request.path,request.params,request.body,request.headers);
        return JsonUtils.parse(result,valueTypeRef);
    }
    public String delete(String path,Map<String,Object> params){
        return execute("delete",path,params,null);
    }
    public String put(String path,Map<String,Object> params, Object request){
        return execute("put",path,params,request);
    }
    public String get(String path, Map<String,Object> params,Map<String,Object> headers){
        try{
            return execute("get",path,params,null,headers);
        }catch (StdException e){
            return null;
        }
    }
    public String get(String path, Map<String,Object> params){
        return get(path, params, Collections.emptyMap());
    }
    public <T> T  post(String path,Map<String,Object> params, Map<String,Object> headers,Object data, TypeReference<T> valueTypeRef){
        String result = post(path, params,headers, data);
        return JsonUtils.parse(result,valueTypeRef);
    }
    public <T> T  post(String path,Map<String,Object> params, Object data, TypeReference<T> valueTypeRef){
        String result = post(path, params, data);
        return JsonUtils.parse(result,valueTypeRef);
    }
    public <T> T  get(String path,Map<String,Object> params ,TypeReference<T> valueTypeRef){
        String result = get(path, params);
        return JsonUtils.parse(result,valueTypeRef);
    }
    public <T> T  get(String path,Map<String,Object> params,Map<String,Object> headers ,TypeReference<T> valueTypeRef){
        String result = get(path, params,headers);
        return JsonUtils.parse(result,valueTypeRef);
    }
    public <T> T  delete(String path,Map<String,Object> params ,TypeReference<T> valueTypeRef){
        String result = delete(path, params);
        return JsonUtils.parse(result,valueTypeRef);
    }
    public static class RequestBuilder{
        Object body;
        Map<String,Object> params;
        Map<String,Object> headers;
        String path;
        private RequestBuilder(){

        }
        public static RequestBuilder create(){
            return new RequestBuilder();
        }
        public RequestBuilder build(){

            return this;
        }
        public RequestBuilder path(String path){
            this.path = path;
            return this;
        }
        public RequestBuilder body(Object body){
            this.body = body;
            return this;
        }
        public RequestBuilder params(Map<String,Object> params){
            this.params = params;
            return this;
        }
        public RequestBuilder headers(Map<String,Object> headers){
            this.headers = headers;
            return this;
        }
    }
}
