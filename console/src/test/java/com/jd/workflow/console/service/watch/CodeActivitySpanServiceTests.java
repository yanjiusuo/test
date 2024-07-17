package com.jd.workflow.console.service.watch;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.entity.watch.CodeActivity;
import com.jd.workflow.console.entity.watch.dto.CodeActivitySpanDto;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CodeActivitySpanServiceTests extends BaseTestCase {
    CodeActivitySpanService spanService = new CodeActivitySpanService();

    private Long start;
    @Before
    public void setUp(){
        start = System.currentTimeMillis();
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
    public void testComputeSpans(){
        List<CodeActivity> activities = buildActivities();
        List<CodeActivitySpanDto> spans = spanService.computeSpans(activities, null);
        assertEquals(2, spans.size());
        assertEquals(60*1000L,(long)spans.get(0).getCostTime());
        assertEquals(60*1000L,(long)spans.get(1).getCostTime());

    }
    @Test
    public void testComputeSpans1(){
        CodeActivity lastActivity = newCodeActivity(-24*60*60*1000L);
        List<CodeActivity> activities = buildActivities();
        List<CodeActivitySpanDto> spans = spanService.computeSpans(activities, lastActivity);
        assertEquals(2, spans.size());
        assertEquals(60*1000L,(long)spans.get(0).getCostTime());
        assertEquals(60*1000L,(long)spans.get(1).getCostTime());

    }
    private Timestamp newTimestamp(Long delta){
        return new Timestamp(start+delta);
    }
    CodeActivitySpanDto newDto(long deltaStart,long deltaEnd){
        CodeActivitySpanDto dto = new CodeActivitySpanDto();
        dto.setStartTime(new Timestamp(deltaStart));
        dto.setEndTime(new Timestamp(deltaEnd));
        dto.setCostTime(deltaEnd-deltaStart);
        return dto;
    }
    @Test
    public void testMergeSpan(){
        List<CodeActivitySpanDto> existSpans = new ArrayList<>();
        List<CodeActivitySpanDto> newSpans = new ArrayList<>();
        existSpans.add(newDto(0,1000L));
        spanService.mergeSpan(existSpans,newSpans);
        assertEquals(1,existSpans.size());

        newSpans.add(newDto(1000L,2000L));
        spanService.mergeSpan(existSpans,newSpans);
        assertEquals(1,existSpans.size());
        assertEquals((long)existSpans.get(0).getCostTime(),2000L);

        newSpans = new ArrayList<>();

        newSpans.add(newDto(16*60*1000L,17*60*1000L));

        spanService.mergeSpan(existSpans,newSpans);

        assertEquals(2,existSpans.size());


    }
}
