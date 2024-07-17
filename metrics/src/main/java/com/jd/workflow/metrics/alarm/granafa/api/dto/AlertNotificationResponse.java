package com.jd.workflow.metrics.alarm.granafa.api.dto;

import lombok.Data;

@Data
public class AlertNotificationResponse extends  AlertNotificationDto{
    Long id;
    String created;
    String updated;
}
