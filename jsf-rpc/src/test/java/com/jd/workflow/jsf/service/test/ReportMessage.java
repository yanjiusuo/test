package com.jd.workflow.jsf.service.test;

import lombok.Data;

@Data
public class ReportMessage <T extends ComMsg> {
    Long id;
    String msg;
    T data;
}
