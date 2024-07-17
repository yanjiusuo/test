package com.jd.workflow.metrics.alarm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.client.TitanRequestClient;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 http://help-titan.jd.com/api/alarm_rule.html#%E7%AE%80%E5%8D%95%E6%8A%A5%E8%AD%A6%E8%A7%84%E5%88%99
 */
@Slf4j
public class TitanAlarmManager implements IAlarmManager{
    TitanRequestClient client = null;
    String source;

    String defaultNotifyGroup = "OjR23n8BaMpmkKTus5NS";

    public void setClient(TitanRequestClient client) {
        this.client = client;
    }

    public void setSource(String source) {
        this.source = source;
    }
    TitanAlertRule buildTitanAlertRuleByUserConfigRule(AlarmRule userConfigRule){
        String ruleName = userConfigRule.getMetric()+"_rule";
        TitanAlertRule titanAlertRule = newAlarmRule(ruleName, userConfigRule.getCallCounterRule());

        if(userConfigRule.getCallCounterRule() != null){
            titanAlertRule.getRules().add(counterRule(userConfigRule.getInterfaceName(),userConfigRule.callCounterRule));
        }
        if(userConfigRule.getTpAlarmRule()!= null){
            if(userConfigRule.getTpAlarmRule().getMax() != null){
                titanAlertRule.getRules().add(tpCountRule(userConfigRule.getInterfaceName(),userConfigRule.tpAlarmRule, "max"));
            }
            if(userConfigRule.getTpAlarmRule().getTp50() != null){
                titanAlertRule.getRules().add(tpCountRule(userConfigRule.getInterfaceName(),userConfigRule.tpAlarmRule, "tp50"));
            }
            if(userConfigRule.getTpAlarmRule().getTp90() != null){
                titanAlertRule.getRules().add(tpCountRule(userConfigRule.getInterfaceName(),userConfigRule.tpAlarmRule, "tp90"));
            }
            if(userConfigRule.getTpAlarmRule().getTp99() != null){
                titanAlertRule.getRules().add(tpCountRule(userConfigRule.getInterfaceName(),userConfigRule.tpAlarmRule, "tp99"));
            }
            if(userConfigRule.getTpAlarmRule().getTp999() != null){
                titanAlertRule.getRules().add(tpCountRule(userConfigRule.getInterfaceName(),userConfigRule.tpAlarmRule, "tp999"));
            }
        }
        if(userConfigRule.getFailAlarmRule() != null){
            titanAlertRule.getRules().add(failCountRule(userConfigRule.getInterfaceName(),userConfigRule.getFailAlarmRule()));
        }

        for (AlertTriggerRule rule : titanAlertRule.getRules()) {
            NotifyPolicy policy = NotifyPolicy.from(userConfigRule.getNotifyInfo());
            policy.setNotifyTemplateGroupId(defaultNotifyGroup);
            rule.setNotifyPolicy(policy);
        }

        return titanAlertRule;
    }
    @Override
    public void addAlarmRule(AlarmRule userConfigRule) {
        String ruleName = userConfigRule.getMetric()+"_rule";
        TitanAlertRule titanAlertRule = buildTitanAlertRuleByUserConfigRule(userConfigRule);

        if(!titanAlertRule.getRules().isEmpty()){

            log.info("titan.begin_add_alarm_rule:ruleId={},data={}",ruleName, JsonUtils.toJSONString(titanAlertRule));
            String result = client.post("/api/v1/alarm-rule/"+source+"/manage/simple/rules",titanAlertRule);
            log.info("titan.add_alarm_rule:ruleId={},data={},result={}",ruleName, JsonUtils.toJSONString(titanAlertRule),result);
        }
    }
    AlertTriggerRule failCountRule(String interfaceName,AlarmRule.FailAlarmRule alarmRule){
        CompareRule rule = new CompareRule();
        rule.setCompareMode("FIXED");
        rule.setCompareType("GREATER_THAN_OR_EQUAL_TO");
        rule.setCompareExp(alarmRule.getSize() +"");

        if(AlarmRule.CounterAlarmType.COUNT.equals(alarmRule.getAlarmType())){
            rule.setQueryExpKey("error");
            rule.setName(interfaceName+"调用失败次数超过"+alarmRule.getSize());
        }else{
            rule.setQueryExpKey("availability");
            rule.setName(interfaceName+"调用失败比例超过"+alarmRule.getSize());
        }
        return AlertTriggerRule.from(alarmRule,rule);
    }
    TitanAlertRule newAlarmRule(String metric, AlarmRule.BaseAlarmRule baseAlarmRule){
        TitanAlertRule alarmRule = new TitanAlertRule();
        alarmRule.setName(metric);
        DataSelect dataSelect = new DataSelect();
        dataSelect.setMetric(metric);
        alarmRule.setDataSelect(dataSelect);
// 命中次数报警阈值：配合 countingPolicy 使用



        return alarmRule;
    }
    AlertTriggerRule tpCountRule(String interfaceName,AlarmRule.TpAlarmRule alarmRule,String propName){
        Double propVal = (Double) BeanTool.getProp(alarmRule, propName);
        if(propVal == null) return null;
        CompareRule rule = new CompareRule();
        rule.setCompareMode("FIXED");
        rule.setCompareType("GREATER_THAN_OR_EQUAL_TO");
        rule.setCompareExp(propVal +"");
        rule.setQueryExpKey(propName);
        rule.setName(interfaceName+propName+"超过"+propVal);

        return  AlertTriggerRule.from(alarmRule,rule);
    }
    AlertTriggerRule counterRule(String interfaceName,AlarmRule.CallCounterRule alarmRule){

        CompareRule rule = new CompareRule();

        rule.setQueryExpKey("total_invokes");
        rule.setCompareMode("FIXED");
        rule.setCompareType("GREATER_THAN_OR_EQUAL_TO");
        rule.setCompareExp(alarmRule.getSize()+"" );
        rule.setName(interfaceName+"调用量超过"+alarmRule.getSize());
        return  AlertTriggerRule.from(alarmRule,rule);
    }
    List<CompareRule> buildCompareRulesByAlertRule(AlarmRule alarmRule){
        List<CompareRule> result = new ArrayList<>();
        if(alarmRule.getCallCounterRule() != null){
            CompareRule rule = new CompareRule();
            rule.setQueryExpKey("total_invokes");
            rule.setCompareMode("FIXED");
            rule.setCompareType("GREATER_THAN_OR_EQUAL_TO");
            rule.setCompareExp(alarmRule.getCallCounterRule().getSize()+"" );
            result.add(rule);
        }
        if(alarmRule.getFailAlarmRule()!= null ){
            CompareRule rule = new CompareRule();
            rule.setQueryExpKey("total_invokes");
            rule.setCompareMode("FIXED");
            rule.setCompareType("GREATER_THAN_OR_EQUAL_TO");
            rule.setCompareExp(alarmRule.getCallCounterRule().getSize()+"" );
            result.add(rule);
        }
        return result;
    }
    public void addRule(){
       // client.post("/api/v1/alarm-rule/"+source+"/manage/simple/rules")
    }

    @Override
    public void updateAlarmRule(AlarmRule alarmRule) {
        String ruleId = getRuleIdByMetric(alarmRule.getMetric());
        String rulePath = "/api/v1/alarm-rule/"+source+"/manage/simple/rules/"+ruleId;
        TitanAlertRule titanAlertRule = buildTitanAlertRuleByUserConfigRule(alarmRule);
        log.info("titan.begin_update_alarm_rule:ruleId={},data={}",ruleId,JsonUtils.toJSONString(titanAlertRule));
        String result = client.put(rulePath,titanAlertRule);
        log.info("titan.success_update_alarm_rule:ruleId={},data={},result={}",ruleId,JsonUtils.toJSONString(titanAlertRule),result);

    }
    String getRuleIdByMetric(String metricId){
        return "35BlXYIBM8IHorbB1Wv7";
    }
    @Override
    public void removeAlarmRule(AlarmRule alarmRule) {
        String ruleId = getRuleIdByMetric(alarmRule.getMetric());
        String rulePath = "/api/v1/alarm-rule/"+source+"/manage/simple/rules/"+ruleId;
        log.info("titan.remove_alarm_rule:source={},ruleId={}",source,ruleId);
        String result = client.delete(rulePath);
        log.info("titan.success_remove_alarm_rule:source={},ruleId={},result={}",source,ruleId,result);
    }

    @Override
    public AlarmList getAlarmList() {
        return null;
    }

    /**
     * http://help-titan.jd.com/api/alarm_rule.html#%E7%AE%80%E5%8D%95%E6%8A%A5%E8%AD%A6%E8%A7%84%E5%88%99
     */
    @Data
    public static class TitanAlertRule {
        String id;
        String name;
        int checkPeriodSec=60;
        DataSelect dataSelect;
        List<AlertTriggerRule> rules = new ArrayList<>();

    }
    @Data
    public static class AlertTriggerRule{
        List<CompareRule> compareRules;
        String countingPolicy;
        String level;
        boolean active = true;
        int countingLimit=1;
        NotifyPolicy notifyPolicy;
        // 告警间隔的秒数
        int alarmIntervalSec;
        public static AlertTriggerRule from(AlarmRule.BaseAlarmRule baseAlarmRule,CompareRule compareRule){
            AlertTriggerRule ret = new AlertTriggerRule();
            ret.compareRules = new ArrayList<>();
            ret.compareRules.add(compareRule);
            ret.level = baseAlarmRule.alarmLevel.name();
            ret.setAlarmIntervalSec(baseAlarmRule.getAlarmIntervalSec());
            ret.setCountingPolicy("CONTINUOUS_COUNTING");
            ret.setCountingLimit(baseAlarmRule.getMatchCount() == 0 ?  1 : baseAlarmRule.getMatchCount() );
            return ret;
        }
    }
    @Data
    public static class CompareRule{
        String queryExpKey;
        String compareMode;
        String compareType;
        String compareExp;
        String name;

        String incrOffsetSec;
    }
    @Data
    public static class DataSelect{
        String metric;
        Map<String,Object> labelEquals = new HashMap<>();
        // 根据titan平台的人说，  dataSelect at least set one label filter. choice from [labelEquals, labelNotEquals, labelMatches, labelNotMatches
        // 因此，这里用一个不存在的label来做过滤操作
        Map<String,Object> labelNotEquals = new HashMap<>();
        public DataSelect(){
            labelNotEquals.put("not_exist_label","11");
        }
    }
    @Data
    public static class NotifyPolicy{
        String receiverSource="EMBEDDED_RECEIVER_MAP";
        ReceiverMap receiverMap;
        List<String> userNotifyMediums = new ArrayList<>();
        String notifyTemplateGroupId;
        public static NotifyPolicy from(AlarmNotifyInfo info){
            NotifyPolicy policy = new NotifyPolicy();
            policy.setReceiverMap(ReceiverMap.from(info));
            if(!info.getEmail().isEmpty()){
                policy.userNotifyMediums.add("EMAIL");
            }
            if(!info.getUserName().isEmpty()){
                policy.userNotifyMediums.add("USERNAME");
            }
            if(!info.getTimline().isEmpty()){
                policy.userNotifyMediums.add("TIMLINE");
            }
            if(!info.getVoiceCall().isEmpty()){
                policy.userNotifyMediums.add("VOICE_CALL");
            }
            return policy;
        }
    }
    @Data
    public static class ReceiverMap{
        @JsonProperty("USERNAME")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<String> USERNAME = new ArrayList<>();
        @JsonProperty("EMAIL")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<String> EMAIL= new ArrayList<>();
        @JsonProperty("VOICE_CALL")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<String> VOICE_CALL= new ArrayList<>();
        @JsonProperty("TIMLINE")
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        List<String> TIMLINE= new ArrayList<>();

        // JDME, TIMLINE, VOICE_CALL, WEBHOOK, INSTANT, SMS, USERNAME, EMAIL

        public static ReceiverMap from(AlarmNotifyInfo info){
            ReceiverMap receiver = new ReceiverMap();
           receiver.setUSERNAME(info.getUserName());
            receiver.setEMAIL(info.getEmail());
             receiver.setTIMLINE(info.getTimline());
           receiver.setVOICE_CALL(info.getVoiceCall());

            return receiver;
        }
    }


}
