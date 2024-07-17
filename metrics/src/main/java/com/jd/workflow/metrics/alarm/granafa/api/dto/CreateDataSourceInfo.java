package com.jd.workflow.metrics.alarm.granafa.api.dto;

import lombok.Data;

@Data
public class CreateDataSourceInfo {
    String name;
    String type;
    String url;
    String access;
    boolean basicAuth;
}
