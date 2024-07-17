package com.jd.workflow.server.dto.interfaceManage;


import java.util.List;

public class JsfHttpMethodModel {
    /**
     * 类型
     */
    String type;

    String methodCode;
    /**
     * 方法id
     */
    Long methodId;
    /**
     * 环境名称
     */
    String envName;
    List<String> authKeys;
    /**
     * 输入
     */
    JsfHttpMethodInput input;
    /**
     * 输出
     */
    JsfHttpMethodOutput output;
    /**
     * 成功条件
     */
    String successCondition;

    String desc;
    String summary;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMethodCode() {
        return methodCode;
    }

    public void setMethodCode(String methodCode) {
        this.methodCode = methodCode;
    }

    public Long getMethodId() {
        return methodId;
    }

    public void setMethodId(Long methodId) {
        this.methodId = methodId;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public List<String> getAuthKeys() {
        return authKeys;
    }

    public void setAuthKeys(List<String> authKeys) {
        this.authKeys = authKeys;
    }

    public JsfHttpMethodInput getInput() {
        return input;
    }

    public void setInput(JsfHttpMethodInput input) {
        this.input = input;
    }

    public JsfHttpMethodOutput getOutput() {
        return output;
    }

    public void setOutput(JsfHttpMethodOutput output) {
        this.output = output;
    }

    public String getSuccessCondition() {
        return successCondition;
    }

    public void setSuccessCondition(String successCondition) {
        this.successCondition = successCondition;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
