package com.jd.workflow.local.entity;

import java.io.Serializable;


/**
 *
 */
public class CommonResult<T> implements Serializable {

    /**
     * 序列化版本uid
     */
    private static final long serialVersionUID = 3281261508261282781L;

    /**
     * 返回值
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 日志跟踪uuid
     */
    private String traceId;
    public CommonResult(){}
    public CommonResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public CommonResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }



    /**
     * 构建code和Msg
     *
     * @param code
     * @param msg
     * @return
     */
    public static CommonResult buildErrorCodeMsg(Integer code, String msg) {
        return new CommonResult(code, msg);
    }

    public static <T> CommonResult buildSuccessResult(T data) {
        return new CommonResult(0, "成功",data);
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
