package com.jd.workflow.metrics.alarm.granafa.api.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlertStateResponse {
    String status;// success
    List<AlertState> data;
}
