package com.jd.workflow.console.service;

import lombok.Data;

@Data
public class LogEntity {
    boolean isError;
    Object msg;
    String businessName;
    String methodName;
    String publishVersion;
}
