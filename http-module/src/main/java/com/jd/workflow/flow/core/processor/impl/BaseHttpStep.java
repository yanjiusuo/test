package com.jd.workflow.flow.core.processor.impl;

import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.exception.StepExecException;
import com.jd.workflow.flow.core.input.HttpInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class BaseHttpStep {
    private String id;
    protected HttpClient httpClient;
    AtomicInteger counter = new AtomicInteger();
    static final Integer connectionTimeout = 5*1000;
    static final Integer readTimeout = 5*1000;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    PoolingHttpClientConnectionManager connectionManager = null;

    TaskDefinition taskDefinition;

    protected  String buildFullUrl(List<String> endpointUrl, String path){
        int size = endpointUrl.size();
        int c = this.counter.updateAndGet((x) -> {
            ++x;
            return x < size ? x : 0;
        });
        return getUrl(endpointUrl.get(c),path);
    }
    /**
     *  将path参数里的/user/{id} path参数替换掉
     * @return
     */

    private String getUrl(String host,String url){
        //String host = metadata.getEndpointUrl().get(0);
        if(url == null) url = "";
        if(!StringUtils.isEmpty(url) &&  !url.startsWith("/")){
            url = "/"+url;
        }
        if(host.endsWith("/")){
            host = host.substring(0,host.length() - 1);
        }
        return host + url;
    }

    protected void setCookie(HttpInput input,StepContext context){
        Map<String,Object> headers = input.getHeaders();
        if(headers == null){
            headers = new HashMap<>();
        }
        String cookie = (String) context.getInput().getHeader("Cookie");
        if(cookie != null && !headers.containsKey("Cookie")){
            headers.put("Cookie",cookie);
        }
        input.setHeaders(headers);
    }

    public BaseHttpStep(){

         connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(500);
        connectionManager.setDefaultMaxPerRoute(200);//例如默认每路由最高50并发，具体依据业务来定

    }

    public void setTaskDefinition(TaskDefinition taskDefinition) {
        this.taskDefinition = taskDefinition;
        initHttpClient(taskDefinition);
    }

    protected void initHttpClient(TaskDefinition taskDef){
        if(taskDef == null
                || taskDef.getTimeout() <= 0
        ){
            RequestConfig requestConfig = RequestConfig.custom()
                    .setConnectTimeout(connectionTimeout)
                    .setConnectionRequestTimeout(readTimeout)
                    .setSocketTimeout(readTimeout)
                    .build();
            HttpClientBuilder builder =HttpClientBuilder.create().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig);
            this.httpClient = builder.build();
            return;
        }


        int timeout = taskDef.getTimeout();
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(timeout)
                .setSocketTimeout(timeout)
                .build();

        HttpClientBuilder builder = HttpClientBuilder.create().setConnectionManager(connectionManager).setDefaultRequestConfig(requestConfig);

        httpClient = builder.build();


    }
    protected boolean isJsonOutput(HttpResponse response) {

        Header header = response.getFirstHeader("Content-Type");
        if(header != null && header.getValue().contains("json")) return true;
        return false;
    }

    protected HttpOutput callHttp( HttpInput httpInput,HttpOutput output){
        HttpEntityEnclosingRequestBase req = new HttpEntityEnclosingRequestBase(){
            @Override
            public String getMethod() {
                String method = httpInput.getMethod();
                if(method.indexOf(",") != -1){
                    method = method.substring(0,method.indexOf(","));
                }
                return method.toUpperCase();
            }
        };
        if(httpInput.getHeaders()!=null){
            for (Map.Entry<String, Object> entry : httpInput.getHeaders().entrySet()) {
                req.setHeader(entry.getKey(), Variant.valueOf(entry.getValue()).toString());
            }
        }

        String url = httpInput.getUrl();
        if(!ObjectHelper.isEmpty(httpInput.getParams())){
            String queryString = StringHelper.encodeQuery(httpInput.getParams(), "utf-8");
            if(url.indexOf("?") != -1){
                url = url + "+&"+queryString;
            }else{
                url = url+"?"+queryString;
            }
        }


        if(httpInput.getBody() != null){

            String content = "";
            ContentType contentType = null;
            if(ReqType.form.equals(httpInput.getReqType())){
                Map<String,Object> params = (Map<String, Object>) httpInput.getBody();
                content = StringHelper.encodeQuery(params,"utf-8");
//                content = StringHelper.markQuery(params);
//                content = URLEncoder.encode(content, StandardCharsets.UTF_8);
                contentType = ContentType.APPLICATION_FORM_URLENCODED;
            }else if(ReqType.json.equals(httpInput.getReqType())){
                content = JsonUtils.toJSONString(httpInput.getBody());
                contentType = ContentType.APPLICATION_JSON;
            }else{
                if(!StringUtils.isEmpty(httpInput.getContentType())){
                    contentType = ContentType.create(httpInput.getContentType(),"utf-8");
                }else{
                    contentType = ContentType.TEXT_XML;
                }
                content = (String) httpInput.getBody();

            }
            StringEntity entity = new StringEntity(content,contentType);
            req.setEntity(entity);
        }

        /*if(isWebService){
            output = new WebServiceOutput();
        }*/
        //output.setInput(httpInput);
        HttpOutput.HttpResponse outputResponse = new HttpOutput.HttpResponse();
        output.setResponse(outputResponse);
        try {
            req.setURI(new URI(url));
        } catch (URISyntaxException e) {
            StdException exception = new StepExecException(getId(), "httpstep.err_invalid_http_uri", e).param("url", httpInput.getUrl());
            output.setException(exception);
            throw exception;
        }

        try {
            HttpResponse response = httpClient.execute(req);
            Header[] allHeaders = response.getAllHeaders();
            for (Header allHeader : allHeaders) {
                outputResponse.getHeaders().put(allHeader.getName(),allHeader.getValue());
            }
            int statusCode = response.getStatusLine().getStatusCode();
            outputResponse.setStatus(statusCode);

            String body = EntityUtils.toString(response.getEntity(),"utf-8");

            if (statusCode > 199 && statusCode < 300) {
                if(isJsonOutput(response)){
                    try{
                        outputResponse.setBody(JsonUtils.parse(body));
                    }catch (Exception e){
                        outputResponse.setBody(body);
                    }
                }else{
                    outputResponse.setBody(body);
                }
            }else{
                outputResponse.setBody(body);
                output.setException(new StepExecException(getId(),"httpstep.err_invalid_response_code").param("statusCode",statusCode).param("body",body));
            }

        }catch (ConnectException e){
            StdException exception = new StepExecException(getId(), "httpstep.err_connect_timeout", e).param("url", httpInput.getUrl());
            output.setException(exception);
        }catch (SocketTimeoutException | ConnectTimeoutException e){
            StdException exception = new StepExecException(getId(), "httpstep.err_req_timeout", e).param("url", httpInput.getUrl());
            output.setException(exception);
        }catch (IOException e) {
            StdException exception = new StepExecException(getId(), "httpstep.err_req_io_exception", e).param("url", httpInput.getUrl());
            output.setException(exception);
        }
        output.copyFromResponse();
        if(output.getException() != null){
            if(output.getException() instanceof RuntimeException){
                throw (RuntimeException)output.getException();
            }else{
                throw (RuntimeException)output.getException();
            }

        }
        return output;
    }

}
