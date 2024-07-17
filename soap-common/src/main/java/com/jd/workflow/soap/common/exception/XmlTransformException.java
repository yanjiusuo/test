package com.jd.workflow.soap.common.exception;

public class XmlTransformException extends StdException{

    public XmlTransformException(String message) {
        super(message,null);
    }
    public XmlTransformException(String message,Throwable cause) {
        super(message,cause);
    }
}
