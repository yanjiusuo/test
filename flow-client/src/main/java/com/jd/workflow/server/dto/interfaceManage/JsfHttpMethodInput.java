package com.jd.workflow.server.dto.interfaceManage;

import java.util.List;


public class JsfHttpMethodInput {
    /**
     * 请求方式 "GET|POST|OPTIONS|...
     */
    String method;
    /**
     * 请求路径
     */
    String url;
    /**
     * 路径参数
     */
    List<JsfJsonType> path;
    /**
     * 参数
     */
    List<JsfJsonType> params;
    /**
     * 表头
     */
    List<JsfJsonType> headers;
    /**
     * 请求格式 post时候用 form|json
     */
    String reqType;
    /**
     * body
     */
    List<JsfJsonType> body;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<JsfJsonType> getPath() {
        return path;
    }

    public void setPath(List<JsfJsonType> path) {
        this.path = path;
    }

    public List<JsfJsonType> getParams() {
        return params;
    }

    public void setParams(List<JsfJsonType> params) {
        this.params = params;
    }

    public List<JsfJsonType> getHeaders() {
        return headers;
    }

    public void setHeaders(List<JsfJsonType> headers) {
        this.headers = headers;
    }

    public String getReqType() {
        return reqType;
    }

    public void setReqType(String reqType) {
        this.reqType = reqType;
    }

    public List<JsfJsonType> getBody() {
        return body;
    }

    public void setBody(List<JsfJsonType> body) {
        this.body = body;
    }
}
