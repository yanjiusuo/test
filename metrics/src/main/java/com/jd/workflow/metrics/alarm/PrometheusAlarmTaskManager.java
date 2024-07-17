package com.jd.workflow.metrics.alarm;

import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.MetricRange;
import com.jd.workflow.metrics.stat.CounterStatResult;
import com.jd.workflow.metrics.stat.HistogramStatResult;
import com.jd.workflow.metrics.stat.PrometheusStatManager;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.StdCalendar;
import org.apache.commons.beanutils.BeanUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
  5分钟轮询一次
 告警功能规则：
   调用量：查询最近x+10分钟内是否超过调用量限制，x是匹配次数
   失败次数：查询x+10 分钟内是否有调用量超出限制

  一分钟一次可能过

 */
public class PrometheusAlarmTaskManager  {
    // 告警间隔时间
    int alarmInterval;
    PrometheusStatManager statManager;
    ExecutorService executorService;

    MetricRange newMetricRange(int matchCount){
        if(matchCount == 0) {
            matchCount = 1;
        }
        StdCalendar now = new StdCalendar();
        MetricRange metricRange = new MetricRange();
        int duration = (matchCount + 10) * 60 * 1000;
        metricRange.setDuration(duration);
        metricRange.setStart(now.toSecondOfMinute(60).getTimeInMillis()-duration);
        metricRange.setStep(60*1000);
        return metricRange;
    }
    /**
     * 执行prometheus告警规则轮询
     * @param alarmRule
     */
    public List<String> queryAlarmRule(AlarmRule alarmRule){
        List<String> errorMsgs = new ArrayList<>();
        executeCounterAlarm(alarmRule,errorMsgs);
        executeTpAlarm(alarmRule,errorMsgs);
        return errorMsgs;
    }
    private void executeTpAlarm(AlarmRule alarmRule,List<String> msgs){
        if(alarmRule.getTpAlarmRule() == null

        ){
            return;
        }
        MetricId metricId = new MetricId();
        metricId.setId(alarmRule.getMetric());
        int counterMatchCount = Math.max(alarmRule.getCallCounterRule().getMatchCount(), alarmRule.getFailAlarmRule().getMatchCount());
        MetricRange metricRange = newMetricRange(counterMatchCount);
        List<HistogramStatResult> results = statManager.queryTp(metricId, metricRange);

        String[] props = new String[]{
                "min",
                "max",
                "tp50",
                "tp90",
                "tp99",
                "tp999"
        };
        for (String prop : props) {
            Double value = (Double) BeanTool.getProp(alarmRule.getTpAlarmRule(),prop);
            int matchCount = alarmRule.getTpAlarmRule().getMatchCount();
            if(value != null){
                boolean exceed = matchAlarm(results,"total", matchCount,value);
                if(exceed){
                    msgs.add(prop+"连续"+matchCount+"次超过"+value);
                }
            }

        }

    }
    private void executeCounterAlarm(AlarmRule alarmRule,List<String> msgs){
        MetricId metricId = new MetricId();
        metricId.setId(alarmRule.getMetric());
        AlarmRule.FailAlarmRule failAlarmRule = alarmRule.getFailAlarmRule();
        AlarmRule.CallCounterRule callCounterRule = alarmRule.getCallCounterRule();
        if(failAlarmRule == null || callCounterRule == null) return;

        int matchCount = failAlarmRule.getMatchCount();
        int counterMatchCount = Math.max(callCounterRule.getMatchCount(), matchCount);
        if(counterMatchCount == 0){
            counterMatchCount = 1;
        }
        MetricRange metricRange = newMetricRange(counterMatchCount);
        List<CounterStatResult> results = statManager.queryCounter(metricId, metricRange);
        if(callCounterRule != null ){

            boolean countExceed = matchAlarm(results,"total", callCounterRule.getMatchCount(), callCounterRule.getSize());
            if(countExceed){
                msgs.add("连续"+counterMatchCount+"次调用量超过"+ callCounterRule.getSize());
            }
        }

        if(failAlarmRule != null){
            boolean totalCountMatch = true;
            if(failAlarmRule.getTotal()!=null){
                totalCountMatch = matchAlarm(results,"total",failAlarmRule.getMatchCount(),failAlarmRule.getTotal());
            }
            if(AlarmRule.CounterAlarmType.COUNT.equals(failAlarmRule.getAlarmType())){
                boolean exceed = matchAlarm(results,"error", matchCount, failAlarmRule.getSize());
                if(totalCountMatch && exceed){
                    msgs.add("连续"+counterMatchCount+"次超过"+ failAlarmRule.getSize());
                }
            }else{
                boolean exceed = matchAlarm(results,"availability", matchCount, failAlarmRule.getSize());
                if(totalCountMatch && exceed){
                    msgs.add("连续"+counterMatchCount+"次超过"+ failAlarmRule.getSize());
                }
            }
        }


    }

    /**
     * 校验 list里的propName属性是否连续 matchCount次超过value
     * @param list
     * @param propName
     * @param matchCount
     * @param value
     * @return
     */
    public boolean matchAlarm(List<?> list, String propName, int matchCount, Number value){
        if(value == null) return false;
        if(matchCount == 0){
            matchCount = 1;
        }
        int matched = 0;
        int maxMatched = 0;
        for (Object o : list) {
            Number number = (Number) BeanTool.getProp(o,propName);
            if(number.doubleValue() >= value.doubleValue()){
                matched++;
                maxMatched = Math.max(matched,maxMatched);
            }else{
                matched = 0;
            }
        }
        if(maxMatched >= matchCount){
            return true;
        }
        return false;


    }

}
