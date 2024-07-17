package com.jd.workflow.soap.example.custom;

import lombok.Data;

import java.util.Map;
import java.util.concurrent.Callable;


public class OperationCallInfo {
    String bindingName;
    String operationName;
    String input;
    Map<String,Object> headers;

    public static void main(String[] args) {

    }
    public String getBindingName() {
        return bindingName;
    }

    public void setBindingName(String bindingName) {
        this.bindingName = bindingName;
    }

    public String getOperationName() {
        return operationName;
    }

    public void setOperationName(String operationName) {
        this.operationName = operationName;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }
}
