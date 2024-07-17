package com.jd.workflow.console.service.watch;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.entity.watch.CodeActivity;
import com.jd.workflow.console.entity.watch.CodeActivityStatistic;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CodeActivityStatisticServiceTests extends BaseTestCase {

    private CodeActivityStatisticService codeActivityStatisticService = new CodeActivityStatisticService();
    private Long start;
    @Before
    public void setUp(){
        start = System.currentTimeMillis();
    }
    @Test
    public void testFetchDayData() {

    }
    private CodeActivity newCodeActivity(Long delta){
        CodeActivity codeActivity= new CodeActivity();
        codeActivity.setTime(new Timestamp(start+delta));
        return codeActivity;
    }
    private List<CodeActivity> buildActivities(){
        List<CodeActivity> activities = new ArrayList<>();
        activities.add(newCodeActivity(0L));
        activities.add(newCodeActivity(1000L));
        activities.add(newCodeActivity(2000L));
        activities.add(newCodeActivity(60*1000L));
        activities.add(newCodeActivity(17*60*1000L));
        activities.add(newCodeActivity(18*60*1000L));
        return activities;
    }
    @Test
    public void testComputeDayCodeCostTime() {
         CodeActivity last = newCodeActivity(-1000L);
        Long costTime = codeActivityStatisticService.computePersonDayCodeCostTime(buildActivities(), last);
        Long expected = 60*1000L+1000+60*1000L;
        assertEquals(expected,costTime);
    }

    @Test
    public void testStatisticDayCodeCostTime() {
        codeActivityStatisticService.statisticDayCodeCostTime(null);
    }


}
