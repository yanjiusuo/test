package com.jd.workflow.metrics.alarm.granafa.api.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AlertNotificationDto {
    String uid;// optional
    String name;// required;
    String type;// required  email、webhook
    boolean isDefault = false;
    boolean sendReminder = false;
    NotificationSetting settings;
    public static interface NotificationSetting{
    }
    @Data
    public static class EmailNotificationSetting implements NotificationSetting{
        String addresses;
        String message;
        String subject;
        boolean singleEmail = true;// 将一封报警邮件发送到所有的收件人
    }
    @Data
    public static class WebhookNotificationSetting implements NotificationSetting{
        String url;
    }
}

