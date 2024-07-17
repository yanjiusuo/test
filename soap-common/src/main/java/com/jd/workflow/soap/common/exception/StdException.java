package com.jd.workflow.soap.common.exception;


import com.jd.workflow.soap.common.util.ObjectHelper;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 标准异常，用来包裹非运行时异常，这样不用每次都处理方法处理异常了，比如
 * try{
 * <p>
 * }catch(e){
 * throw StdException.adapt("xxxx",e);
 * }
 * 出现StdException说明方法内部调用失败了
 */
public class StdException extends RuntimeException {
    String msg;
    Integer code;
    Object data;
    boolean formatPrams = true;
    protected Map<String, Object> params = new LinkedHashMap<>();

    public StdException(String msg) {
        super(msg);
        this.msg = msg;

    }
    public StdException(Throwable e) {
        super(e);
    }

    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if(formatPrams && !ObjectHelper.isEmpty(params)){
            msg+="[params="+params.toString()+"]";
        }

        return msg;
    }

    public StdException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;

    }
    public static RuntimeException adapt(Throwable throwable) {
        return (RuntimeException) (throwable instanceof RuntimeException ? (RuntimeException) throwable : new StdException( throwable));
    }

    public static RuntimeException adapt(String msg, Throwable throwable) {
        return (RuntimeException) (throwable instanceof RuntimeException ? (RuntimeException) throwable : new StdException(msg, throwable));
    }
    public StdException param(String name,Object value){
        params.put(name,value);
        return  this;
    }
    public String getMsg() {
        return msg;
    }

    public StdException code(Integer code){
        this.code = code;
        return this;
    }
    public StdException data(Object data){
        this.data = data;
        return this;
    }

    public Integer getCode() {
        return code;
    }

    public Object getData() {
        return data;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public boolean isFormatPrams() {
        return formatPrams;
    }

    public void setFormatPrams(boolean formatPrams) {
        this.formatPrams = formatPrams;
    }
}
