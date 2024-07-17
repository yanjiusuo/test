package com.jd.workflow.server.dto.interfaceManage;



public class JsfDocConfigDto {
    String invokeConfig;
    String pomConfig;
    /**
     * 文档类型：md、html
     */
    String docType;

    public JsfDocConfigDto() {
    }

    public String getInvokeConfig() {
        return invokeConfig;
    }

    public void setInvokeConfig(String invokeConfig) {
        this.invokeConfig = invokeConfig;
    }

    public String getPomConfig() {
        return pomConfig;
    }

    public void setPomConfig(String pomConfig) {
        this.pomConfig = pomConfig;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }
}
