package com.jd.workflow.metrics.stat;

import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.MetricRange;
import com.jd.workflow.metrics.client.PrometheusReqClient;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * https://prometheus.io/docs/prometheus/latest/querying/api/
 *
 * https://stackoverflow.com/questions/48966107/is-there-a-way-to-add-prometheus-monitoring-targets-and-alerts-programmatically
 */
public class PrometheusStatManager implements IMetricStatManager{
    PrometheusReqClient client;
    static final int SECONDS = 1000;
    ExecutorService executorService;

    public void setClient(PrometheusReqClient client) {
        this.client = client;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public List<CounterStatResult> queryCounter(MetricId metricId, MetricRange metricRange) {
        return queryCounterStat(metricId,metricRange);
    }

    /**
     sum by (is_success) (round(increase(requests_latency_seconds_count[1m])))
     * @param metricId
     * @param range
     */
    List<CounterStatResult> queryCounterStat(MetricId metricId,MetricRange range){
        String queryExpr = "sum by (is_success) (round(increase(%s_count[%sm])))";
        CounterResult counterResult =queryRangeData(queryExpr,range);

        CounterResultData data = counterResult.getData();
        return transformStatResultToCounter(data,range);
    }
    CounterResult queryRangeData(String query,MetricRange range){
        Map<String,Object> params = newParams(range);



        params.put("query",query);

        String response = client.execute("/api/v1/query_range", params);
        if(StringUtils.isBlank(response)){
            throw new BizException("查询失败");
        }
        CounterResult counterResult = JsonUtils.parse(response, CounterResult.class);
        if(!"success".equals(counterResult.getStatus())){
            throw new BizException("查询失败");
        }
        return counterResult;
    }
    Map<String,Object> newParams(MetricRange range){
        Map<String,Object> params = new HashMap<>();

        params.put("start",range.getStart()/SECONDS);
        params.put("end",(range.getStart()+range.getDuration())/SECONDS);
        params.put("step",range.getStep()/SECONDS);
        return params;
    }
    List<CounterStatResult> transformStatResultToCounter(CounterResultData data, MetricRange range){
        List<CounterStatResult> list = range.getSteps().stream().map(vs->{
            CounterStatResult result = new CounterStatResult();
            result.setTimestamp(vs);
            return result;
        }).collect(Collectors.toList());

        for (MetricResult metricResult : data.getResult()) {
            fillDataItem(list,metricResult);
        }
        return list;
    }

    void fillDataItem(List<CounterStatResult> list, MetricResult metricResult){
        boolean isSuccess = "1".equals(metricResult.getMetric().get("is_success"));
        int j = 0;
        for (int i = 0; i < list.size(); i++) {
            CounterStatResult current = list.get(i);
            long currentSecond = current.getTimestamp()/SECONDS;
            Object[] values = metricResult.getValues().get(j);
            long statTimestamp = Variant.valueOf(values[0]).toLong();
            if(currentSecond == statTimestamp){

                int value = Variant.valueOf(values[1]).toInt();
                if(isSuccess){
                    current.setSuccess(value);
                }else{
                    current.setError(value);
                }
                j++;
                if(metricResult.getValues().size() <= j){
                    break;
                }
            }
        }
    }
    static final String QUERY_TP_EXPR = "histogram_quantile(%s, sum by (le) (rate(%s_bucket[%ss])))";

    /**
     {
     "status": "success",
     "data": {
     "resultType": "matrix",
     "result": [{
     "metric": {},
     "values": [[1658393780.600, "NaN"], [1658393840.600, "0.06625000000000002"], [1658393900.600, "NaN"], [1658393960.600, "NaN"], [1658394020.600, "NaN"], [1658394080.600, "NaN"], [1658394140.600, "NaN"], [1658394200.600, "NaN"], [1658394260.600, "NaN"], [1658394320.600, "NaN"], [1658394380.600, "NaN"], [1658394440.600, "NaN"], [1658394500.600, "NaN"], [1658394560.600, "NaN"], [1658394620.600, "NaN"], [1658394680.600, "NaN"], [1658394740.600, "NaN"], [1658394800.600, "NaN"], [1658394860.600, "NaN"], [1658394920.600, "NaN"], [1658394980.600, "NaN"], [1658395040.600, "NaN"], [1658395100.600, "NaN"], [1658395160.600, "NaN"], [1658395220.600, "NaN"], [1658395280.600, "NaN"], [1658395340.600, "NaN"], [1658395400.600, "NaN"], [1658395460.600, "NaN"], [1658395520.600, "NaN"], [1658395580.600, "NaN"]]
     }
     ]
     }
     }

     * @param tp
     * @param metricId
     * @param range
     * @param results
     */
    void queryTp(Tp tp,MetricId metricId,MetricRange range,List<HistogramStatResult> results){
        String queryExpr = String.format(QUERY_TP_EXPR,tp.percent,metricId.getId(),range.getDuration()/SECONDS);
        CounterResult counterResult = queryRangeData(queryExpr, range);
        int i = 0;
        List<Object[]> values = counterResult.getData().getResult().get(0).getValues();
        for (HistogramStatResult result : results) {
            Object[] objs = values.get(i);
            if(result.getTimestamp()/SECONDS == Variant.valueOf(objs[0]).toLong()){
                if(!"NaN".equals(objs[1])){
                    try {
                        BeanUtils.setProperty(result,tp.getName(),Variant.valueOf(objs[1]).toDouble());
                    } catch (Exception e) {
                        throw StdException.adapt(e);
                    }
                }
                i++;
                if(values.size() <= i){
                    break;
                }
            }
        }
    }
    @Override
    public List<HistogramStatResult> queryTp(MetricId metricId, MetricRange range) {
        double[] needQueryExpr = new double[]{0.000001,0.5,0.9,0.99,0.999,1.0};
        Tp[] tps = new Tp[]{
                new Tp(0.000001,"min"),
                new Tp(0.5,"tp50"),
                new Tp(0.9,"tp90"),
                new Tp(0.99,"tp99"),
                new Tp(0.999,"tp999"),
        };
        List<HistogramStatResult> results = range.getSteps().stream().map(vs -> {
            HistogramStatResult statResult = new HistogramStatResult();
            statResult.setTimestamp(vs);
            return statResult;
        }).collect(Collectors.toList());
        List<CompletableFuture<Object>> futures = new ArrayList<>();
        for (Tp tp : tps) {
            CompletableFuture<Object> future = CompletableFuture.supplyAsync(() -> {
                PrometheusStatManager.this.queryTp(tp, metricId, range, results);
                return null;
            },executorService);
            futures.add(future);
        }

        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get();
        } catch (Exception e) {
            throw new BizException("查询失败");
        }
        return results;
    }

    /**
     {
         "status": "success",
         "data": {
             "resultType": "matrix",
             "result": [{
                 "metric": {
                 "is_success": "0"
                     },
                    "values": [[1658369451.704, "2"], [1658369511.704, "0"]]
                 }, {
                     "metric": {
                       "is_success": "1"
                     },
                   "values": [[1658367651.704, "0"], [1658367711.704, "4"], [1658367771.704, "0"]]
                 }
             ]
         }
     }

     */
    
    
    @Data
    public static class CounterResult{
        String status;
        CounterResultData data;
    }
    @Data
    public static class CounterResultData{
        String resultType;
        List<MetricResult> result;
    }
    @Data
    public static class MetricResult{
        Map<String,Object> metric;
        List<Object[]> values;
    }
    @Data
    static class Tp{
        public Tp(double percent, String name) {
            this.percent = percent;
            this.name = name;
        }

        double percent;
        String name;
    }
}
