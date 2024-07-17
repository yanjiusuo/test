package com.jd.workflow.metrics.stat;

import com.jd.workflow.metrics.Label;
import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.MetricRange;
import com.jd.workflow.metrics.client.TitanRequestClient;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;


import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TitanStatManager implements IMetricStatManager{
    String defaultToken = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOnsibmFtZSI6ImRlbW8iLCJyb2xlcyI6WyJTT1VSQ0VfT1dORVI6ZGVtbyJdfSwiaWF0IjoxNjI5MzU5MDY5LCJpc3MiOiJ0aXRhbiJ9.Qylk4yn2Uni4twiylY3ufWdplnTlgh4VdO0p_6Oinp60-NksZ3U40w12y0oomcw1bTcPPw7zJmui2oi6L1Vn-Q";

    TitanRequestClient client;
    String source;
    public TitanStatManager(){
        this.client = new TitanRequestClient(defaultToken);
        this.source = "demo";
    }

    public void setClient(TitanRequestClient client) {
        this.client = client;
    }

    Map<String,Object> buildDataSelect(MetricId metricId, MetricRange range){
        Map<String,Object> dataSelect = new LinkedHashMap<>();
        dataSelect.put("metric",metricId.getId());

        Map<String,String> labelEquals = new LinkedHashMap<>();
        for (Label label : metricId.getLabels()) {
            labelEquals.put(label.getKey(),label.getValue());
        }

        Map<String,Object> result = new LinkedHashMap<>();
        result.put("dataSelect",dataSelect);
        result.put("labelEquals",labelEquals);
        result.put("beginTimestamp",range.getStart());
        result.put("duration",range.getDuration());
        result.put("minStep",range.getStep());
        return result;
    }
    public List<MetricValue> queryRange(MetricId metricId, MetricRange metricRange){
        try {
            String result = client.post("/api/v1/metric-query/" + source + "/range", buildDataSelect(metricId, metricRange));
            if(result == null){
                return null;
            }

            List<RangeResult> rangeResult = JsonUtils.parseArray(result, RangeResult.class);
            List<MetricValue> metricValues = extractWatchResult(rangeResult,metricRange);
            return metricValues;
        } catch (Exception e) {
            throw  StdException.adapt(e);
        }
    }
    List<MetricValue> extractWatchResult(List<RangeResult> results, MetricRange range){
        List<MetricValue> result = new ArrayList<>();
        for (int i = 0; i <= range.getDuration() / range.getStep(); i++) {
            MetricValue metricValue = new MetricValue();
            metricValue.timestamp = range.getStart() + range.getStep()*i;
            result.add(metricValue);
        }
        for (RangeResult rangeResult : results) {
            fillValue(result,rangeResult);
        }
        return result;
    }
    void fillValue(List<MetricValue> values,RangeResult result){
        String subTypeName = (String) result.getLabels().get("_SUB");
        if(StringUtils.isEmpty(subTypeName)){
            return;
        }
        SUBTYPE subtype = SUBTYPE.valueOf(subTypeName);
        if(subtype == null){
            return;
        }
        int j = 0;
        for (int i = 0; i < values.size(); i++) {

            MetricValue metricValue = values.get(i);
            Object[] watchValue = result.getValues().get(j);
            long timestamp = Variant.valueOf(watchValue[0]).toLong();
            double count = Variant.valueOf(watchValue[1]).toDouble();
            if(timestamp/1000 == metricValue.timestamp/1000){ //微秒可能不相等，但是秒级别的统计是相等的
                try {
                    BeanUtils.setProperty(metricValue,subTypeName,count);
                } catch (Exception e) {
                   throw StdException.adapt(e);
                }
                j++;
                if(result.getValues().size() <= j ){
                    break;
                }
            }
        }
    }

    @Override
    public List<CounterStatResult> queryCounter(MetricId metricId, MetricRange metricRange) {

        List<MetricValue> metricValues = queryRange(metricId, metricRange);
        if(metricValues == null){
            return Collections.emptyList();
        }
        return metricValues.stream().map(vs->{
            CounterStatResult result = new CounterStatResult();
            result.timestamp = vs.timestamp;
            result.error = Variant.valueOf(vs.error).toInt();
            result.success = Variant.valueOf(vs.success).toInt();
            result.total = Variant.valueOf(vs.invokes).toInt();
            return result;
        }).collect(Collectors.toList());
    }

    @Override
    public List<HistogramStatResult> queryTp(MetricId metricId, MetricRange metricRange) {
        List<MetricValue> metricValues = queryRange(metricId, metricRange);

        return metricValues.stream().map(vs->{
            HistogramStatResult result = new HistogramStatResult();
            result.timestamp = vs.timestamp;
            result.min = vs.min;
            result.max = vs.max;
            result.tp50 = vs.tp50w;
            result.tp90 = vs.tp90w;
            result.tp99 = vs.tp99w;
            result.tp999 = vs.tp999w;

            return result;
        }).collect(Collectors.toList());
    }
    @Data
    public static class MetricValue{
        long timestamp;
        double invokes;
        double success;
        double error;
        double min;
        double max;
        double tp50w;
        double tp90w;
        double tp99w;
        double tp999w;
    }
    @Data
    public static class RangeResult {
        Integer step;
        Long startTimestamp;
        Long endTimestamp;
        List<Object[]> values;

        /**
         "server": "10.10.10.11:6483",
         "cluster": "jim://3044266746474140587/6683",
         "_GBY": "nil",
         "__name__": "wjf_stat_delayz",
         "_MT": "simp",
         "client": "10.0.218.152",
         "_SRC": "demo",
         "_IVS": "60",
         "_SUB": "error",
         "client_id": "1"
         _开头的是元描述
         */
        Map<String,Object> labels;
        Map<String,Object> rawLabels;
    }

    public static enum SUBTYPE{
        error,invokes,max,min,success,totalElapsed,tp50w,tp90w,tp999w,tp99w
    }
}
