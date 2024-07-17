package com.jd.workflow.metrics.alarm.granafa.api.dto;

import lombok.Data;

@Data
public class DataSourceInfo {
    Long id;
    String uid;
    String orgId;
    String name;
    String type;
    String typeLogoUrl;
    String access;
    String url;
    String password;
    String user;
    String database;
    boolean basicAuth;
}
