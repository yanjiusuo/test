package com.jd.workflow.service;

import lombok.Data;

@Data
public class UserDefineException extends RuntimeException{
    int code;
    String message;
    String data;
}
