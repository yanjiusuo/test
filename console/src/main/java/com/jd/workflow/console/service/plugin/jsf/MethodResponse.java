package com.jd.workflow.console.service.plugin.jsf;

public class MethodResponse {

    private String[] inputParam;

    private String methodName;

    private String outputParam;

    public String[] getInputParam() {
        return inputParam;
    }

    public void setInputParam(String[] inputParam) {
        this.inputParam = inputParam;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getOutputParam() {
        return outputParam;
    }

    public void setOutputParam(String outputParam) {
        this.outputParam = outputParam;
    }
}
