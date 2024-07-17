package com.jd.workflow.soap.common.exception;

public class BizException extends StdException{
    public BizException(String msg) {
        super(msg);
    }
    public BizException(String msg,Throwable e) {
        super(msg,e);
    }
}
