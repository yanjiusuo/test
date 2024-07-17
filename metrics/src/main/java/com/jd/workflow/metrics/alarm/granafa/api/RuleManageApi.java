package com.jd.workflow.metrics.alarm.granafa.api;

import com.jd.workflow.metrics.alarm.granafa.api.dto.GrafanaAlarmRule;
import com.jd.workflow.metrics.client.GrafanaClient;
import com.jd.workflow.soap.common.util.JsonUtils;

/**
 grafana用来管理告警规则的api:  https://grafana.com/docs/grafana/latest/developers/http_api/alerting_provisioning/#route-get-alert-rule
 */
public class RuleManageApi extends BaseApi{
    public GrafanaAlarmRule getRule(String uid){
        String result = client.get("/api/v1/provisioning/alert-rules/" + uid, null);
        return JsonUtils.parse(result, GrafanaAlarmRule.class);
    }
    public GrafanaAlarmRule createRule(GrafanaAlarmRule rule){
        String result = client.post("/api/v1/provisioning/alert-rules", null,rule);
        return JsonUtils.parse(result, GrafanaAlarmRule.class);
    }
    public GrafanaAlarmRule updateRule(GrafanaAlarmRule rule){
        String result = client.put("/api/v1/provisioning/alert-rules/"+rule.getUid(), null,rule);
        return JsonUtils.parse(result, GrafanaAlarmRule.class);
    }
    public GrafanaAlarmRule removeRule(String uid){
        String result = client.delete("/api/v1/provisioning/alert-rules/"+uid);
        return JsonUtils.parse(result, GrafanaAlarmRule.class);
    }
}
