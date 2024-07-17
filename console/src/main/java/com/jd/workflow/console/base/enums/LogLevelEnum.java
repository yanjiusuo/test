package com.jd.workflow.console.base.enums;

public enum LogLevelEnum {
    EXCEPTION(1, "异常"), NONE(0, "正常");

    private int code;
    private String info;
    LogLevelEnum(int code, String info) {
        this.code = code;
        this.info = info;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
