package com.jd.workflow.metrics.alarm.granafa.api.dto;

import com.jd.workflow.metrics.alarm.PrometheusAlarmManager;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.Map;

@Builder
public class GrafanaAlarmExpr {
    String expr;
    String datasourceUid;
    String metric;
    String tpName;
    String tpCnName;
    String tpAllowValue;
    //Integer forSeconds;
    Integer matchCount;
    String opType;// and、or
    public Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition> buildAlert(){
        String alertRule = "{\"refId\":\"{refId}\",\"datasourceUid\":\"{datasourceUid}\",\"queryType\":\"\",\"relativeTimeRange\":{\"from\":300,\"to\":0},\"model\":{\"refId\":\"{refId}\",\"__type\":\"query\",\"hide\":false,\"editorMode\":\"builder\",\"expr\":\"histogram_quantile({tp}, sum by(le) (rate({metric}_bucket[1m])))\",\"legendFormat\":\"__auto\",\"range\":true,\"interval\":\"60\",\"format\":\"table\"}}";
        String conditionTemplate = "{\"type\":\"query\",\"evaluator\":{\"params\":[{tpValue}],\"type\":\"gt\"},\"operator\":{\"type\":\"or\"},\"query\":{\"params\":[\"{tpName}\"]},\"reducer\":{\"type\":\"min\",\"params\":[]}}";
        Map<String,Object> args = new HashMap<>();
        args.put("refId",tpName);// tp50、tp99等参数
        args.put("datasourceUid",datasourceUid);

        args.put("metric",metric);

        alertRule = StringHelper.replacePlaceholder(alertRule,args);
        GrafanaAlarmRule.AlertQuery query = JsonUtils.parse(alertRule,GrafanaAlarmRule.AlertQuery.class);
        if(matchCount ==null || matchCount <= 0){
            matchCount  = 1;
        }
        query.getRelativeTimeRange().setFrom(matchCount*60);
        GrafanaAlarmRule.AlertQueryModel queryModel = (GrafanaAlarmRule.AlertQueryModel) query.getModel();
        queryModel.setExpr(expr);

        Map<String,Object> condArgs = new HashMap<>();
        condArgs.put("tpName",tpName);
        condArgs.put("tpValue",tpAllowValue);
        String conditionStr = StringHelper.replacePlaceholder(conditionTemplate,condArgs);

        GrafanaAlarmRule.ClassicCondition condition = JsonUtils.parse(conditionStr,GrafanaAlarmRule.ClassicCondition.class);
        String template = " 指标名:{tpName},配置值：{tpValue},实际:{{ $values."+ PrometheusAlarmManager.DEFAULT_CONDITION_NAME+"{index}.Value }} ";

        Map<String,Object> templateArgs = new HashMap<>();
        templateArgs.put("tpName",tpCnName == null ? tpName : tpCnName);
        templateArgs.put("tpValue",tpAllowValue);

        condition.setSummaryInfo(StringHelper.replacePlaceholder(template,templateArgs));
        if(opType!=null){
            condition.getOperator().put("type",opType);
        }
        return Pair.of(query,condition);
    }
}
