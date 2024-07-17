package com.jd.workflow.metrics.alarm.granafa.api.dto;

import lombok.Data;

@Data
public class DataSourceResponse {
    Long id;
    String message;
    String name;
    DataSourceInfo datasource;
}
