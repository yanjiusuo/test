package com.jd.workflow.server.dto.interfaceManage;


import java.util.List;

public class JsfHttpMethodOutput {
    /**
     * 表头
     */
    List<JsfJsonType> headers;
    /**
     *
     */
    List<JsfJsonType> body;

    public JsfHttpMethodOutput() {
    }

    public List<JsfJsonType> getHeaders() {
        return headers;
    }

    public void setHeaders(List<JsfJsonType> headers) {
        this.headers = headers;
    }

    public List<JsfJsonType> getBody() {
        return body;
    }

    public void setBody(List<JsfJsonType> body) {
        this.body = body;
    }
}
