package com.jd.workflow.soap.common.exception;

public final class StdAdaptException extends StdException {
    private static final long serialVersionUID = -8877286400206485097L;

    StdAdaptException(Throwable throwable) {
        this("std.err_fail", throwable);
    }
    StdAdaptException(String msg,Throwable throwable){
        super(msg,throwable);
    }
    public boolean isWrapException() {
        return true;
    }
}
