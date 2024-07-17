package com.jd.workflow.soap.schema;

import lombok.Data;

@Data
public class UserDefineException extends RuntimeException{
    int code;
    String message;
    String data;
}
