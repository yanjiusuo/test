package com.jd.workflow.metrics.stat;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.MetricRange;
import com.jd.workflow.metrics.client.PrometheusReqClient;
import com.jd.workflow.metrics.client.TitanRequestClient;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static org.mockito.Matchers.any;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class PrometheusStatManagerTests extends BaseTestCase {
    @InjectMocks
    PrometheusStatManager prometheusStatManager;
    @Mock
    PrometheusReqClient client;
    MetricId metricId;

    @Before
    public void setUp(){
       // titanStatManager = new TitanStatManager();

       // client = Mockito.mock(TitanRequestClient.class);
        metricId = new MetricId();
        metricId.setId("abc");
        ThreadPoolExecutor executor =  new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>());
        prometheusStatManager.setExecutorService(executor);
    }
    MetricId newMetricId(String id){
        MetricId metricId = new MetricId();
        metricId.setId(id);
        return metricId;
    }
    MetricRange newRange(){
        MetricRange metricRange = new MetricRange();
        metricRange.setStart(1658452200000L);
        metricRange.setStep(60000);
        metricRange.setDuration(6000000);
        return metricRange;
    }
    @Test
    public void testQueryRange(){
        MetricRange metricRange=new MetricRange();
        metricRange.setDuration(600000);
        metricRange.setStep(60000);
        metricRange.setStart(1659339000000L);
        String summaryResult = getResourceContent("classpath:titan/prometheus-counter-result.json");
        //when(requestClient.post(any(),any())).thenReturn(summaryResult);
        Mockito.doAnswer(vs->{
            return summaryResult;
        }).when(client).execute(any(),any());
        List<CounterStatResult> counter = prometheusStatManager.queryCounter(newMetricId("counter"), metricRange);
        log.info("result={}",JsonUtils.toJSONString(counter));
    }
    @Test
    public void testQueryTp(){
        MetricRange metricRange=new MetricRange();
        metricRange.setDuration(600000);
        metricRange.setStep(60000);
        metricRange.setStart(1659339000000L);
        String summaryResult = getResourceContent("classpath:titan/prometheus-tp-result.json");
        //when(requestClient.post(any(),any())).thenReturn(summaryResult);
        Mockito.doAnswer(vs->{
            return summaryResult;
        }).when(client).execute(any(),any());
        List<HistogramStatResult> counter = prometheusStatManager.queryTp(newMetricId("counter"), metricRange);
        log.info("result={}",JsonUtils.toJSONString(counter));
    }
}
