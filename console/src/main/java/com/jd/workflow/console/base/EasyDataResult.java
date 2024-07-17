package com.jd.workflow.console.base;

import java.io.Serializable;

public class EasyDataResult<T> implements Serializable {
    /**
     * 返回值
     */
    private Integer status;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T result;

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public EasyDataResult() {}

    public EasyDataResult(Integer status, String message, T result) {
        this.status = status;
        this.message = message;
        this.result = result;
    }

    public EasyDataResult(Integer status, String message) {
        this.status = status;
        this.message = message;
    }
}
