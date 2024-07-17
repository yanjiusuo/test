package com.jd.workflow.metrics.stat;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.metrics.MetricId;
import com.jd.workflow.metrics.MetricRange;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class TitanStatManagerTests extends BaseTestCase {
    @InjectMocks
    TitanStatManager titanStatManager;
    @Mock
    TitanRequestClient client;
    MetricId metricId;

    @Before
    @Override
    public void setUp(){
       // titanStatManager = new TitanStatManager();

       // client = Mockito.mock(TitanRequestClient.class);
        metricId = new MetricId();
        metricId.setId("abc");
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
        metricRange.setDuration(6000000);
        metricRange.setStep(60000);
        metricRange.setStart(1658452200000L);
        String summaryResult = getResourceContent("classpath:titan/summary-result.json");
        //when(requestClient.post(any(),any())).thenReturn(summaryResult);
        Mockito.doAnswer(vs->{
            return summaryResult;
        }).when(client).post(any(),any());
        List<CounterStatResult> counter = titanStatManager.queryCounter(newMetricId("counter"), newRange());
        log.info("result={}",JsonUtils.toJSONString(counter));
    }
    @Test
    public void testQueryTp(){
        MetricRange metricRange=new MetricRange();
        metricRange.setDuration(6000000);
        metricRange.setStep(60000);
        metricRange.setStart(1658452200000L);
        String summaryResult = getResourceContent("classpath:titan/summary-result.json");
        //when(requestClient.post(any(),any())).thenReturn(summaryResult);
        Mockito.doAnswer(vs->{
            return summaryResult;
        }).when(client).post(any(),any());
        List<HistogramStatResult> counter = titanStatManager.queryTp(newMetricId("counter"), newRange());
        log.info("result={}",JsonUtils.toJSONString(counter));
    }

}
