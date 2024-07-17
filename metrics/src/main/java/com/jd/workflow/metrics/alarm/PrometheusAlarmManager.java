package com.jd.workflow.metrics.alarm;

import com.jd.workflow.metrics.alarm.granafa.api.RuleManageApi;
import com.jd.workflow.metrics.alarm.granafa.api.dto.GrafanaAlarmExpr;
import com.jd.workflow.metrics.alarm.granafa.api.dto.GrafanaAlarmRule;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
  由于prometheus不支持通过api动态添加规则，通过grafana来实现告警配置：https://grafana.com/docs/grafana/latest/developers/http_api/alerting_provisioning/

 */
@Slf4j
public class PrometheusAlarmManager implements IAlarmManager{
    static String avgTemplate = "sum(increase({metric}_sum[1m]))/sum(increase({metric}_count[1m]))";
    static String histogramTemplate = "histogram_quantile({tp}, sum by(le) (rate({metric}_bucket[1m])))";
    static String callCounterTemplate = "sum(increase({metric}_count[1m]))";
    static String fallCounterTemplate = "sum(increase({metric}_count{is_success=\"0\"}[1m]))";
    static String failCounterPercentTemplate = "sum(increase({metric}_count{is_success=\"0\"}[1m]))/sum(increase({metric}_count[1m]))";
    public static String DEFAULT_CONDITION_NAME = "condition";

    RuleManageApi ruleManageApi;
    String defaultPrometheusDataSourceUid = "5rCaoQz4z";
    String folderUid = "test-folder1";
    String defaultCallbackUrl ="http://localhost:8020/alarm/callback";

    public void setRuleManageApi(RuleManageApi ruleManageApi) {
        this.ruleManageApi = ruleManageApi;
    }

    @Override
    public void addAlarmRule(AlarmRule alarmRule) {
        List<GrafanaAlarmRule> rules = alarmRuleToGrafana(alarmRule);
        log.info("grafana.get_alarm_rule:rules={}",JsonUtils.toJSONString(rules));
        //System.out.println(123);
        if(!rules.isEmpty()){
            for (GrafanaAlarmRule rule : rules) {
                GrafanaAlarmRule existRule = ruleManageApi.getRule(rule.getUid());
                if(existRule != null){
                    ruleManageApi.updateRule(rule);
                }else{
                    ruleManageApi.createRule(rule);
                }
            }
        }
    }

    List<GrafanaAlarmRule> alarmRuleToGrafana(AlarmRule alarmRule){
        List<GrafanaAlarmRule> result = new ArrayList<>();
        AlarmRule.TpAlarmRule tpAlarmRule = alarmRule.getTpAlarmRule();
        String[][] tpMetrics = new String[][]{
                {"max","1.0"},
                {"tp50","0.5"},
                {"tp90","0.9"},
                {"tp99","0.99"},
                {"tp999","0.999"}
        };
        List<Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition>> list = new ArrayList<>();
        for (String[] tpMetric : tpMetrics) {
            Double value = (Double) BeanTool.getProp(tpAlarmRule, tpMetric[0]);
            if(value != null){
                Map<String,Object> args = new HashMap<>();
                args.put("tp",tpMetric[1]);
                args.put("metric",alarmRule.getMetric());
                Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition> pair =GrafanaAlarmExpr.builder()
                        .datasourceUid(defaultPrometheusDataSourceUid)
                        .metric(alarmRule.getMetric())
                        .tpName(tpMetric[0])
                        .tpAllowValue(value+"")
                        .expr(StringHelper.replacePlaceholder(histogramTemplate,args))
                        .matchCount(tpAlarmRule.getMatchCount()).build().buildAlert();
                list.add(pair);
            }

        }
        if(tpAlarmRule.getAvg() != null){
            Map<String,Object> args = new HashMap<>();
            args.put("metric", alarmRule.getMetric());
            Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition> pair = GrafanaAlarmExpr.builder()
                                                                                        .datasourceUid(defaultPrometheusDataSourceUid)
                                                                                        .metric(alarmRule.getMetric())
                                                                                        .tpName("avg")
                                                                                        .tpAllowValue(tpAlarmRule.getAvg()+"")
                                                                                        .expr(StringHelper.replacePlaceholder(avgTemplate,args))
                                                                                        .matchCount(tpAlarmRule.getMatchCount()).build().buildAlert();
            list.add(pair);
        }
        if(!list.isEmpty()){
            result.add(newGrafanaTpAlarmRule(alarmRule,alarmRule.getMetric()+"_tp_metric",alarmRule.getTpAlarmRule(), list));
        }
        if(alarmRule.getCallCounterRule() !=null && alarmRule.getCallCounterRule().getSize() != null){
            Map<String,Object> args = new HashMap<>();
            args.put("metric", alarmRule.getMetric());
            Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition> pair =GrafanaAlarmExpr.builder()
                    .datasourceUid(defaultPrometheusDataSourceUid)
                    .metric(alarmRule.getMetric())
                    .tpName("callCounter")
                    .tpCnName("调用量")
                    .tpAllowValue(alarmRule.getCallCounterRule().getSize()+"")
                    .expr(StringHelper.replacePlaceholder(callCounterTemplate,args))
                    .matchCount(tpAlarmRule.getMatchCount()).build().buildAlert();

            result.add(newGrafanaTpAlarmRule(alarmRule,alarmRule.getMetric()+"_callCounter",alarmRule.getCallCounterRule(), Collections.singletonList(pair)));
        }
        AlarmRule.FailAlarmRule failRule = alarmRule.getFailAlarmRule();
        if(failRule != null && failRule.getSize() != null){
            Map<String,Object> args = new HashMap<>();
            args.put("metric",alarmRule.getMetric());
            List<Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition>> failList = new ArrayList();
            Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition> failCount = null;
            if(AlarmRule.CounterAlarmType.COUNT.equals(failRule.alarmType)){
                failCount = GrafanaAlarmExpr.builder()
                        .datasourceUid(defaultPrometheusDataSourceUid)
                        .metric(alarmRule.getMetric())
                        .tpName("failCount")
                        .tpCnName("调用失败次数")
                        .opType("and")
                        .tpAllowValue(failRule.getSize() + "")
                        .expr(StringHelper.replacePlaceholder(fallCounterTemplate,args))
                        .matchCount(failRule.getMatchCount()).build().buildAlert();
            }else{
                failCount = GrafanaAlarmExpr.builder()
                        .datasourceUid(defaultPrometheusDataSourceUid)
                        .metric(alarmRule.getMetric())
                        .tpName("failPercent")
                        .tpCnName("调用失败比例")
                        .opType("and")
                        .tpAllowValue(failRule.getSize() + "")
                        .expr(StringHelper.replacePlaceholder(failCounterPercentTemplate,args))
                        .matchCount(failRule.getMatchCount()).build().buildAlert();
            }
            failList.add(failCount);
            if(failRule.getTotal() != null){
                Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition> callCounterAlarm = GrafanaAlarmExpr.builder()
                        .datasourceUid(defaultPrometheusDataSourceUid)
                        .metric(alarmRule.getMetric())
                        .tpName("callCounter")
                        .tpCnName("调用量")
                        .opType("and")
                        .tpAllowValue(failRule.getTotal() + "")
                        .expr(newCounterRule(alarmRule.getMetric()))
                        .matchCount(tpAlarmRule.getMatchCount()).build().buildAlert();
                failList.add(callCounterAlarm);

            }
            result.add(newGrafanaTpAlarmRule(alarmRule,alarmRule.getMetric()+"failCount",failRule,failList));
        }
        return  result;
    }
    String newCounterRule(String metric){
        Map<String,Object> args = new HashMap<>();
        args.put("metric", metric);
        return StringHelper.replacePlaceholder(callCounterTemplate,args);
    }
    private GrafanaAlarmRule newGrafanaTpAlarmRule(AlarmRule alarmRule,String uid, AlarmRule.BaseAlarmRule baseAlarmRule, List<Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition>> list) {
        GrafanaAlarmRule grafanaTpAlarmRule = new GrafanaAlarmRule();
        String summary = "[在线联调]"+alarmRule.getInterfaceName()+"出现异常";


        String conditionRefId = DEFAULT_CONDITION_NAME;
        GrafanaAlarmRule.AlertQuery conditionQuery = new GrafanaAlarmRule.AlertQuery();
        conditionQuery.setDatasourceUid("-100");
        conditionQuery.setRefId(conditionRefId);


        GrafanaAlarmRule.ClassicConditionsModel conditionsModel = new GrafanaAlarmRule.ClassicConditionsModel();
        conditionsModel.getDatasource().put("uid","-100");
        conditionsModel.getDatasource().put("type","__expr__");
        conditionsModel.setRefId(conditionRefId);

        for (Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition> pair : list) {
            conditionsModel.getConditions().add(pair.getRight());

            grafanaTpAlarmRule.getData().add(pair.getLeft());

            //conditionQuery.setRelativeTimeRange(pair.getLeft().getRelativeTimeRange());
        }
        grafanaTpAlarmRule.getAnnotations().put("summary",conditionsModel.buildSummary(summary));
        grafanaTpAlarmRule.getAnnotations().put("runbook_url",defaultCallbackUrl);
        conditionQuery.setModel(conditionsModel);

        grafanaTpAlarmRule.getData().add(conditionQuery);

        grafanaTpAlarmRule.setUid(uid);
        grafanaTpAlarmRule.setOrgID(1L);

        grafanaTpAlarmRule.setFolderUID(folderUid);
        grafanaTpAlarmRule.setForNanos(baseAlarmRule.getAlarmIntervalSec()*1000000000L );
        grafanaTpAlarmRule.setRuleGroup("tp_group");
        grafanaTpAlarmRule.setTitle(uid);
        grafanaTpAlarmRule.setCondition(conditionRefId);
        grafanaTpAlarmRule.setNoDataState(GrafanaAlarmRule.NoDataState.OK);
        //if(AlarmLevel.WARNING.equals(baseAlarmRule.getAlarmLevel()) ){
            grafanaTpAlarmRule.setExecErrState(GrafanaAlarmRule.ExecErrState.Alerting);
        /*}else{
            grafanaTpAlarmRule.setExecErrState(GrafanaAlarmRule.ExecErrState.Error);
        }*/

        grafanaTpAlarmRule.getLabels().put("metric", alarmRule.getMetric());
        return grafanaTpAlarmRule;
    }

    /*Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition> newAlert(String dataSourceUid, String metric, String tpName, String tpPercent,String tpAllowValue,String expr){
        String alertRule = "{\"refId\":\"{refId}\",\"datasourceUid\":\"{datasourceUid}\",\"queryType\":\"\",\"relativeTimeRange\":{\"from\":300,\"to\":0},\"model\":{\"refId\":\"{refId}\",\"__type\":\"query\",\"hide\":false,\"editorMode\":\"builder\",\"expr\":\"histogram_quantile({tp}, sum by(le) (rate({metric}_bucket[1m])))\",\"legendFormat\":\"__auto\",\"range\":true,\"interval\":\"60\",\"format\":\"table\"}}";
        String conditionTemplate = "{\"type\":\"query\",\"evaluator\":{\"params\":[{tpValue}],\"type\":\"gt\"},\"operator\":{\"type\":\"or\"},\"query\":{\"params\":[\"{tpNam}\"]},\"reducer\":{\"type\":\"max\",\"params\":[]}}";
        Map<String,Object> args = new HashMap<>();
        args.put("refId",tpName);// tp50、tp99等参数
        args.put("datasourceUid",dataSourceUid);

        args.put("metric",metric);
        args.put("tp",tpPercent);
        StringHelper.replacePlaceholder(alertRule,args);
        GrafanaAlarmRule.AlertQuery query = JsonUtils.parse(alertRule,GrafanaAlarmRule.AlertQuery.class);
        if(expr != null){
            GrafanaAlarmRule.AlertQueryModel queryModel = (GrafanaAlarmRule.AlertQueryModel) query.getModel();
            queryModel.setExpr(expr);
        }
        Map<String,Object> condArgs = new HashMap<>();
        condArgs.put("tpName",tpName);
        condArgs.put("tpValue",tpAllowValue);
        GrafanaAlarmRule.ClassicCondition condition = JsonUtils.parse(conditionTemplate,GrafanaAlarmRule.ClassicCondition.class);
        return Pair.of(query,condition);
    }
    Pair<GrafanaAlarmRule.AlertQuery, GrafanaAlarmRule.ClassicCondition> newAlert(String dataSourceUid, String metric, String tpName, String tpPercent,String tpAllowValue){
       return newAlert(dataSourceUid, metric, tpName, tpPercent, tpAllowValue,null);
    }*/

    public void listRules(){

    }
    @Override
    public void updateAlarmRule(AlarmRule alarmRule) {
        addAlarmRule(alarmRule);
    }

    @Override
    public void removeAlarmRule(AlarmRule alarmRule) {
        List<GrafanaAlarmRule> rules = alarmRuleToGrafana(alarmRule);
        for (GrafanaAlarmRule rule : rules) {
            ruleManageApi.removeRule(rule.getUid());
        }
    }

    @Override
    public AlarmList getAlarmList() {
        return null;
    }
}
