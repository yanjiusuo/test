package com.jd.workflow.jsf.input;

import com.jd.workflow.flow.core.input.BaseInput;

import java.util.LinkedHashMap;
import java.util.Map;

public class JsfInput extends BaseInput {
    public Object body;
    Map<String,String> headers = new LinkedHashMap<>();




    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }
}
