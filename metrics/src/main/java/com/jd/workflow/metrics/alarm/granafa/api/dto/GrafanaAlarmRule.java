package com.jd.workflow.metrics.alarm.granafa.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 {

 "execErrState": "Alerting",
 "folderUID": "uVi7Ifk4z",
 "noDataState": "NoData",
 "condition": "B",
 "orgID": 33,
 "ruleGroup": "group1",
 "title": "tp95",
 "data": [{
 "refId": "A",
 "queryType": "",
 "relativeTimeRange": {
 "from": 900,
 "to": 0
 },
 "datasourceUid": "S5LgZgg4k",
 "model": {
 "editorMode": "builder",
 "expr": "histogram_quantile(0.95, sum by(le) (rate(requests_latency_mill_seconds_bucket[1m])))",
 "hide": false,
 "intervalMs": 1000,
 "legendFormat": "__auto",
 "maxDataPoints": 43200,
 "range": true,
 "refId": "A"
 }
 }, {
 "refId": "B",
 "queryType": "",
 "relativeTimeRange": {
 "from": 0,
 "to": 0
 },
 "datasourceUid": "-100",
 "model": {
 "conditions": [{
 "evaluator": {
 "params": [
 0.001
 ],
 "type": "gt"
 },
 "operator": {
 "type": "and"
 },
 "query": {
 "params": [
 "A"
 ]
 },
 "reducer": {
 "params": [],
 "type": "last"
 },
 "type": "query"
 }
 ],
 "datasource": {
 "type": "__expr__",
 "uid": "-100"
 },
 "downsampler": "mean",
 "hide": false,
 "intervalMs": 1000,
 "maxDataPoints": 43200,
 "refId": "B",
 "type": "classic_conditions",
 "upsampler": "fillna"
 }
 }
 ],
 "for": 300000000000,
 "annotations": {
 "description": "fdsfdsdfsdsfdfs",
 "runbookUrl": "http://localhost:6010/echo",
 "summary": "sdfdfsdsfdsfdsfdsf"
 },
 "labels": {}
 }

 */
@Data
public class GrafanaAlarmRule {
    long id;
    String uid;
    Long orgID;// required 组织结构id
    String folderUID;// required 文件夹id
    String ruleGroup;// required; 分组groupid
    String title;// required 标题
    String condition;// required;  哪个data项目是条件
    List<AlertQuery> data = new ArrayList<>(); // required
    NoDataState noDataState;// required;  没有数据的状态
    ExecErrState execErrState;// required 执行错误的状态

    Map<String,String> annotations = new HashMap<>();
    Map<String,String> labels = new HashMap<>();
    @JsonProperty("for")
    long forNanos;
    @Data
    public static class RelativeTimeRange{
        Integer from=0;
        Integer to=0;

        public RelativeTimeRange() {
        }

        public RelativeTimeRange(Integer from, Integer to) {
            this.from = from;
            this.to = to;
        }
    }
    @Data
    public static class AlertQuery{
        String datasourceUid;
        Model model;
        String refId;
        String queryType;
        RelativeTimeRange relativeTimeRange;
    }
    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME,property = "__type",visible = true)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ClassicConditionsModel.class, name = "classic_conditions"),
            @JsonSubTypes.Type(value = AlertQueryModel.class, name = "query")
    })
    @Data
    public static abstract class Model{
        // 仅仅用来做json解析使用，并没有实际意义
        @JsonIgnore
        String __type;
    }
    @Data
    public static class ClassicConditionsModel extends Model{
        String refId;
        boolean hide=false;
        String type="classic_conditions";
        Map<String,String > datasource = new HashMap<>();
        List<ClassicCondition> conditions = new ArrayList<>();

        public String buildSummary(String prefix){
            for (int i = 0; i < conditions.size(); i++) {
                Map<String,Object> args = new HashMap<>();
                args.put("index",i);
                prefix+= StringHelper.replacePlaceholder(conditions.get(i).getSummaryInfo(),args);
            }
            return prefix;
        }
    }
    //{"refId":"B","hide":false,"type":"classic_conditions","datasource":{"uid":"-100","type":"__expr__"},"conditions":[{"type":"query","evaluator":{"params":[0.03],"type":"gt"},"operator":{"type":"and"},"query":{"params":["A"]},"reducer":{"type":"max","params":[]}}]}
    @Data
    public static class ClassicCondition{
        String type = "query";
        Map<String,Object> evaluator = new HashMap<>();//{"params": [0.03],"type": "gt"}
        Map<String,Object> operator = new HashMap<>();// {type:"and"}
        Map<String,Object> query = new HashMap<>();// {params:["A"]}
        Map<String,Object> reducer=new HashMap<>(); // {type:max,"params":[] }

        String summaryInfo;
    }
    @Data
    public static class AlertQueryModel extends Model{
        String editorMode;
        String expr;

        boolean hide;
        String interval; // 秒级别的interval
        Integer intervalMs=1000; // 秒级别的interval
        String legendFormat;
        String format;
        Integer maxDataPoints;
        boolean range;
        String refId;
    }

    public static enum NoDataState{
        NoData,Alerting,OK
    }
    public static enum ExecErrState{
        OK,Alerting,Error
    }
}
