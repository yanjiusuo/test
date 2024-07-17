package com.jd.workflow.metrics.client;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.metrics.alarm.AlarmRule;
import com.jd.workflow.metrics.alarm.PrometheusAlarmManager;
import com.jd.workflow.metrics.alarm.granafa.api.AlertingApi;
import com.jd.workflow.metrics.alarm.granafa.api.DataSourceApi;
import com.jd.workflow.metrics.alarm.granafa.api.FolderApi;
import com.jd.workflow.metrics.alarm.granafa.api.RuleManageApi;
import com.jd.workflow.metrics.alarm.granafa.api.dto.*;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

@Slf4j

public class GrafanaClientTests extends BaseTestCase {
    GrafanaClient client = new GrafanaClient();

    PrometheusAlarmManager prometheusAlarmManager = new PrometheusAlarmManager();

    RuleManageApi ruleManageApi =  new RuleManageApi();
    DataSourceApi dataSourceApi =  new DataSourceApi();
    AlertingApi alertingApi =  new AlertingApi();
    FolderApi folderApi = new FolderApi();
    @Before
    public void before(){
        ruleManageApi.setClient(client);
        dataSourceApi.setClient(client);
        prometheusAlarmManager.setRuleManageApi(ruleManageApi);
        alertingApi.setClient(client);
        folderApi.setClient(client);
    }
    @Test
    public void testFolder(){
        String result = client.get("/api/folders", null);
    }

    /**
     * {"id":2,"uid":"Y-OR2wkVz","orgId":1,"name":"Prometheus-1","type":"prometheus","typeLogoUrl":"","access":"proxy","url":"http://localhost:9090","user":"","database":"","basicAuth":false,"basicAuthUser":"","withCredentials":false,"isDefault":false,"jsonData":{"httpMethod":"POST"},"secureJsonFields":{},"version":1,"readOnly":false,"accessControl":{"alert.instances.external:read":true,"alert.instances.external:write":true,"alert.notifications.external:read":true,"alert.notifications.external:write":true,"alert.rules.external:read":true,"alert.rules.external:write":true,"datasources.id:read":true,"datasources:delete":true,"datasources:query":true,"datasources:read":true,"datasources:write":true}}
     */
    @Test
    public void createPrometheus(){
        CreateDataSourceInfo info = new CreateDataSourceInfo();
        info.setName("default-prometheus-2");
        info.setType("prometheus");
        info.setUrl("http://localhost:9090");
        info.setAccess("proxy");
        info.setBasicAuth(false);
        DataSourceResponse response = dataSourceApi.create(info);
        System.out.println(response);//DataSourceResponse(id=1, message=Datasource added, name=default-prometheus-1, datasource=DataSourceInfo(id=1, orgId=1, name=default-prometheus-1, type=prometheus, typeLogoUrl=, access=proxy, url=http://localhost:9090, password=null, user=, database=, basicAuth=false))

    }

    @Test
    public void testHealthCheck(){
        DataSourceHealthCheckResponse response = dataSourceApi.checkHealthById("1");
        System.out.println(response);//DataSourceResponse(id=3, message=Datasource added, name=default-prometheus, datasource=DataSourceInfo(id=3, orgId=1, name=default-prometheus, type=promethues, typeLogoUrl=, access=proxy, url=http://localhost:9090, password=null, user=, database=, basicAuth=false))

    }
    @Test
    public void testCreateFolder(){
        String uid = "test-folder1";
        Folder folder = new Folder();
        folder.setUid(uid);
        folder.setTitle("metric_folder");
        FolderResponse response = folderApi.createFolder(folder);
        log.info("folder.create_folder:folder={},response={}", JsonUtils.toJSONString(folder),JsonUtils.toJSONString(response));
    }
    @Test
    public void testRemoveFolder(){
        String uid = "test-folder1";

        FolderResponse response = folderApi.deleteFolderByUid(uid);
        log.info("folder.create_folder:uid={},response={}", uid,response);
    }

    public void testCreateRule(){
        createRule("tp_90_alert_rule");
    }

    public void createRule(String ruleUid){
        String dataSourceUid = "5rCaoQz4z";
        GrafanaAlarmRule rule = new GrafanaAlarmRule();
        rule.setUid(ruleUid);
        rule.setOrgID(1L);
        rule.setFolderUID("folder1");
        rule.setRuleGroup("group1");
        rule.setCondition("B");
        rule.setTitle("tp99");
        rule.setForNanos(5*60*1000000000); // 5分钟内不重复告警
        rule.setNoDataState(GrafanaAlarmRule.NoDataState.NoData);
        rule.setExecErrState(GrafanaAlarmRule.ExecErrState.Alerting);
        GrafanaAlarmRule.AlertQuery tpAlert = newAlert(dataSourceUid,"requests_latency_mill_seconds","0.9");
        GrafanaAlarmRule.AlertQuery conditionQuery = newCondition(0.001);
        rule.setData(new ArrayList<>());
        rule.getData().add(tpAlert);
        rule.getData().add(conditionQuery);

        ruleManageApi.createRule(rule);
    }
    GrafanaAlarmRule.AlertQuery newCondition(double tpMinValue){
        GrafanaAlarmRule.AlertQuery condition = new GrafanaAlarmRule.AlertQuery();
        condition.setRefId("B");
        condition.setDatasourceUid("-100");

        condition.setRelativeTimeRange(new GrafanaAlarmRule.RelativeTimeRange(300,0));
        String conditionModel = "{\"refId\":\"B\",\"hide\":false,\"type\":\"classic_conditions\",\"datasource\":{\"uid\":\"-100\",\"type\":\"__expr__\"},\"conditions\":[{\"type\":\"query\",\"evaluator\":{\"params\":[0.03],\"type\":\"gt\"},\"operator\":{\"type\":\"and\"},\"query\":{\"params\":[\"A\"]},\"reducer\":{\"type\":\"max\",\"params\":[]}}]}";
        GrafanaAlarmRule.ClassicConditionsModel alertQueryModel  = JsonUtils.parse(conditionModel,GrafanaAlarmRule.ClassicConditionsModel.class);
        List<Double> params  = (List<Double>) alertQueryModel.getConditions().get(0).getEvaluator().get("params");
        params.set(0,tpMinValue);
        condition.setModel(alertQueryModel);

        return condition;
    }
    GrafanaAlarmRule.AlertQuery newAlert(String dataSourceUid,String metric,String tpInfo){
        GrafanaAlarmRule.AlertQuery tpAlert = new GrafanaAlarmRule.AlertQuery();
        tpAlert.setDatasourceUid(dataSourceUid);
        tpAlert.setRefId("A");
        tpAlert.setRelativeTimeRange(new GrafanaAlarmRule.RelativeTimeRange(300,0));

        GrafanaAlarmRule.AlertQueryModel alertQueryModel = new GrafanaAlarmRule.AlertQueryModel();
        alertQueryModel.setRefId("A");
        alertQueryModel.setHide(false);
        alertQueryModel.setEditorMode("builder");
        String exprTemplate = "histogram_quantile(%s, sum by(le) (rate(%s_bucket[1m])))";//"histogram_quantile(0.9, sum by(le) (rate(requests_latency_mill_seconds_bucket[1m])))"
        String expr = String.format(exprTemplate,tpInfo,metric);
        alertQueryModel.setExpr(expr);
        alertQueryModel.setRange(true);
        alertQueryModel.setLegendFormat("__auto");
        alertQueryModel.setInterval(60+"");
        alertQueryModel.setFormat("table");
        tpAlert.setModel(alertQueryModel);
        return tpAlert;
    }
    @Test
    public void testQueryAlert(){
        AlertQuery query = new AlertQuery();
       // query.setLimit(1000);
        query.getState().add("alerting");
        query.getState().add("pending");
        List<AlertingResponse> response = alertingApi.query(query);
        System.out.println(JsonUtils.toJSONString(response));
    }

    @Test
    public void testCreateAlarmRule(){
        AlarmRule alarmRule = newAlarmRule();
        prometheusAlarmManager.addAlarmRule(alarmRule);
    }
    @Test
    public void testRemoveAlarmRule(){
        AlarmRule alarmRule = newAlarmRule();
        prometheusAlarmManager.removeAlarmRule(alarmRule);
    }
    AlarmRule newAlarmRule(){
        AlarmRule alarmRule = new AlarmRule();
        alarmRule.setInterfaceName("aaa");
        alarmRule.setMetric("requests_latency_mill_seconds");
        alarmRule.setTpAlarmRule(newTpAlarmRule());
        alarmRule.setFailAlarmRule(newFailAlarmRule());
        alarmRule.setCallCounterRule(newCallCounterRule());
        return alarmRule;
    }

    private AlarmRule.CallCounterRule newCallCounterRule() {
        AlarmRule.CallCounterRule callCounterRule = new AlarmRule.CallCounterRule();
        callCounterRule.setSize(3);
        callCounterRule.setAlarmIntervalSec(2*60);
        return callCounterRule;
    }

    private AlarmRule.FailAlarmRule newFailAlarmRule() {
        AlarmRule.FailAlarmRule failAlarmRule = new AlarmRule.FailAlarmRule();
        failAlarmRule.setSize(2);
        failAlarmRule.setAlarmIntervalSec(2*60);
        failAlarmRule.setTotal(2);
        return failAlarmRule;
    }

    private AlarmRule.TpAlarmRule newTpAlarmRule() {
        AlarmRule.TpAlarmRule tpAlarmRule = new AlarmRule.TpAlarmRule();
        tpAlarmRule.setAlarmIntervalSec(2*60);
        tpAlarmRule.setAvg(12.0);
        tpAlarmRule.setMax(13.1);
        tpAlarmRule.setTp50(1.1);
        tpAlarmRule.setTp90(1.3);
        tpAlarmRule.setTp99(2.3);
        tpAlarmRule.setTp999(1.2);
        return tpAlarmRule;
    }

}
