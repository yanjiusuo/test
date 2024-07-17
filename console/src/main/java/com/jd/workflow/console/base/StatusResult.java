package com.jd.workflow.console.base;

import lombok.Data;

import java.io.Serializable;

import static com.jd.workflow.console.base.enums.ServiceErrorEnum.COMMON_EXCEPTION;

/**
 *
 */
@Data
public class StatusResult<T> implements Serializable {

    /**
     * 序列化版本uid
     */
    private static final long serialVersionUID = 3281261508261282781L;

    /**
     * 返回值:200为成功，非200为失败
     */
    private Integer status;

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
    public StatusResult(){}
    public StatusResult(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    public StatusResult(Integer status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
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
     * 构建status和Msg
     *
     * @param status
     * @param msg
     * @return
     */
    public static StatusResult buildErrorstatusMsg(Integer status, String msg) {
        return new StatusResult(status, msg);
    }


    public static <T> StatusResult buildSuccessResult(T data) {
        return new StatusResult(200, "成功",data);
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
