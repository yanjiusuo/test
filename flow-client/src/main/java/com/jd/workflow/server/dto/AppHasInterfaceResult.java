package com.jd.workflow.server.dto;

public class AppHasInterfaceResult {
    String jdosAppCode;
    boolean hasInterface;

    public String getJdosAppCode() {
        return jdosAppCode;
    }

    public AppHasInterfaceResult(String jdosAppCode, boolean hasInterface) {
        this.jdosAppCode = jdosAppCode;
        this.hasInterface = hasInterface;
    }

    public AppHasInterfaceResult() {
    }

    public void setJdosAppCode(String jdosAppCode) {
        this.jdosAppCode = jdosAppCode;
    }

    public boolean isHasInterface() {
        return hasInterface;
    }

    public void setHasInterface(boolean hasInterface) {
        this.hasInterface = hasInterface;
    }
}
