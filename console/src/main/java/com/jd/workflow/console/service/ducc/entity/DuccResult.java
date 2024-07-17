package com.jd.workflow.console.service.ducc.entity;

/**
 * DuccResult
 *
 * @author wangxianghui6
 * @date 2022/3/1 3:40 PM
 */
public class DuccResult<T> {
    private Integer code;
    private Integer status;
    private String message;
    private T data;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
