package com.jd.workflow.metrics.alarm.granafa.api;

import com.jd.workflow.metrics.alarm.granafa.api.dto.AlertNotificationDto;
import com.jd.workflow.metrics.alarm.granafa.api.dto.AlertNotificationResponse;
import com.jd.workflow.soap.common.util.JsonUtils;

import java.util.Map;

public class NotificationApi extends BaseApi{
    public AlertNotificationResponse create(AlertNotificationDto dto){
        String response = client.post("/api/alert-notifications",null, dto);
        return JsonUtils.parse(response,AlertNotificationResponse.class);
    }
    public AlertNotificationResponse update(AlertNotificationDto dto){
        String response = client.put("/api/alert-notifications/uid/"+dto.getUid(),null, dto);
        return JsonUtils.parse(response,AlertNotificationResponse.class);
    }

    public AlertNotificationResponse getByUid(String uid){
        String response = client.get("/api/alert-notifications/uid/"+uid,null);
        return JsonUtils.parse(response,AlertNotificationResponse.class);
    }

    public Map<String,Object> deleteByUid(AlertNotificationDto dto){
        String response = client.delete("/api/alert-notifications/uid/"+dto.getUid());
        return JsonUtils.parse(response,Map.class);
    }
}
