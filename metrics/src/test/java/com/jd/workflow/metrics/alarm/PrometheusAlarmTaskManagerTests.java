package com.jd.workflow.metrics.alarm;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.metrics.stat.HistogramStatResult;
import com.jd.workflow.metrics.stat.PrometheusStatManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class PrometheusAlarmTaskManagerTests extends BaseTestCase {
    @InjectMocks
    PrometheusAlarmTaskManager taskManager;
    @Mock
    PrometheusStatManager statManager;
    @Test
    public void matchAlarmTest(){
        List<HistogramStatResult> results  = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            HistogramStatResult result = new HistogramStatResult();
            result.setTp99(Double.valueOf(i));
            results.add(result);
        }
        boolean match = taskManager.matchAlarm(results, "tp99", 2, 3);
        assertEquals(true,match);
    }
}
