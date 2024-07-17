package com.jd.workflow.metrics.alarm.granafa.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AlertState {
    Map<String,Object> labels;
    Map<String,Object> annotations;
    String state;//NoData、Pending
    String activeAt;
    String value;
}
