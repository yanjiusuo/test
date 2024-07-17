package com.jd.workflow.metrics.alarm.granafa.api;

import com.jd.workflow.metrics.alarm.granafa.api.dto.AlertQuery;
import com.jd.workflow.metrics.alarm.granafa.api.dto.AlertStateResponse;
import com.jd.workflow.metrics.alarm.granafa.api.dto.AlertingResponse;
import com.jd.workflow.soap.common.util.JsonUtils;

import java.util.List;
import java.util.Map;

/**
 当前告警信息查询,参考文档：https://grafana.com/docs/grafana/latest/developers/http_api/alerting/
 */
public class AlertingApi extends BaseApi{
    @Deprecated
    public List<AlertingResponse> query(AlertQuery query){
        Map params = JsonUtils.cast(query, Map.class);
        String result = client.get("/api/alerts", params);
        return JsonUtils.parseArray(result,AlertingResponse.class);
    }
    @Deprecated
    public AlertingResponse getAlertById(String id){

        String result = client.get("/api/alerts/"+id, null);
        return JsonUtils.parse(result,AlertingResponse.class);
    }
    /**
     * https://editor.swagger.io/?url=https://raw.githubusercontent.com/grafana/grafana/main/pkg/services/ngalert/api/tooling/post.json
     * 查询所有告警的响应信息
     */
    public AlertStateResponse queryAllAlertState(){
        String s = client.get("/api/prometheus/grafana/api/v1/alerts", null);
        return JsonUtils.parse(s, AlertStateResponse.class);

    }
}
