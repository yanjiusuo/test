package com.jd.workflow.soap.exception;

import com.jd.workflow.soap.common.exception.StdException;

public class WsdlGenerateException extends StdException {
    public WsdlGenerateException(String msg) {
        super(msg);
    }

    public WsdlGenerateException(String msg, Throwable e) {
        super(msg, e);
    }
}
