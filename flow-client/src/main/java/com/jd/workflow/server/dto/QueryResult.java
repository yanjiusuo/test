package com.jd.workflow.server.dto;

import java.io.Serializable;
import java.util.UUID;

public class QueryResult<T> implements Serializable {

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
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 日志跟踪uuid
     */
    private String traceId;
    public QueryResult(){}
    public QueryResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public QueryResult(Integer code, String message, T data) {
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
    public static QueryResult buildErrorCodeMsg(Integer code, String msg) {
        return new QueryResult(code, msg);
    }
    public static QueryResult error(String msg) {
        return new QueryResult(400, msg);
    }

    public static <T> QueryResult<T> buildSuccessResult(T data) {
        return new QueryResult(0, "成功",data);
    }

    public String getTraceId() {
        if(traceId!=null)  {
            return traceId;
        }
        return UUID.randomUUID().toString();
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}