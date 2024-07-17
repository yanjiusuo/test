package com.jd.workflow.metrics.alarm;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.metrics.client.TitanRequestClient;
import org.junit.Before;
import org.junit.Test;


public class TitanAlarmManagerTests extends BaseTestCase {
    TitanAlarmManager titanAlarmManager;
    TitanRequestClient requestClient;
    @Before
    public void before(){
        requestClient = new TitanRequestClient("eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJhdWQiOnsibmFtZSI6ImRlbW8iLCJyb2xlcyI6WyJTT1VSQ0VfT1dORVI6ZGVtbyJdfSwiaWF0IjoxNjI5MzU5MDY5LCJpc3MiOiJ0aXRhbiJ9.Qylk4yn2Uni4twiylY3ufWdplnTlgh4VdO0p_6Oinp60-NksZ3U40w12y0oomcw1bTcPPw7zJmui2oi6L1Vn-Q");
        titanAlarmManager = new TitanAlarmManager();
        titanAlarmManager.setClient(requestClient);
        titanAlarmManager.setSource("demo");
        titanAlarmManager.setClient(requestClient);
    }
    AlarmRule newAlarmRule(){
        String metric = "wjf_stat_delayz";
        AlarmRule alarmRule = new AlarmRule();
        alarmRule.setMetric(metric);

        AlarmRule.CallCounterRule callCounterRule = new AlarmRule.CallCounterRule();
        callCounterRule.setSize(1);
        callCounterRule.setAlarmLevel(AlarmLevel.CRITICAL);
        callCounterRule.setAlarmType(AlarmRule.CounterAlarmType.COUNT);
        alarmRule.setCallCounterRule(callCounterRule);

        AlarmNotifyInfo notifyInfo = new AlarmNotifyInfo();
        notifyInfo.getTimline().add("wangjingfang3");
        alarmRule.setNotifyInfo(notifyInfo);
        return alarmRule;
    }
    @Test
    public void testAddAlarmRule(){
        titanAlarmManager.addAlarmRule(newAlarmRule());
    }
    public void testUpdateRule(){
        titanAlarmManager.updateAlarmRule(newAlarmRule());
    }

    @Test
    public void testRemoveAlarmRule(){
        titanAlarmManager.removeAlarmRule(newAlarmRule());
    }
}
