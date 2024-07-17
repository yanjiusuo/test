package com.jd.workflow.console.dto.test.deeptest;


import java.io.Serializable;

import static com.jd.workflow.console.base.enums.ServiceErrorEnum.COMMON_EXCEPTION;

/**
 *
 */
public class TestResult<T> implements Serializable {

    /**
     * 序列化版本uid
     */
    private static final long serialVersionUID = 3281261508261282781L;

    /**
     * 返回值:0为成功，非0为失败
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 日志跟踪uuid
     */
    private String traceId;
    public TestResult(){}
    public TestResult(Integer code, String message) {
        this.code = code;
        this.msg = message;
    }

    public TestResult(Integer code, String message, T data) {
        this.code = code;
        this.msg = message;
        this.data = data;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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
    public static TestResult buildErrorCodeMsg(Integer code, String msg) {
        return new TestResult(code, msg);
    }
    public static TestResult error(String msg) {
        return new TestResult(COMMON_EXCEPTION.getCode(), msg);
    }

    public static <T> TestResult buildSuccessResult(T data) {
        return new TestResult(1, "成功",data);
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public boolean isSuccess(){
        return code.equals(1);
    }
}

