package com.jd.workflow.console.service.watch;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.entity.watch.dto.CodeActivityDto;
import org.junit.Before;
import org.junit.Test;

import java.security.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CodeActivityServiceTests extends BaseTestCase {

    private CodeActivityStatisticService codeActivityStatisticService = new CodeActivityStatisticService();
    private Long start;
    CodeActivityService codeActivityService = new CodeActivityService();
    @Before
    public void setUp(){
        start = System.currentTimeMillis();
    }

    CodeActivityDto newDto(Long delta,String filePath){
        CodeActivityDto dto = new CodeActivityDto();

        dto.setTime(start+delta);
        dto.setFilePath(filePath);
        dto.setEventType("fileSave");
        return dto;
    }
    @Test
    public void testLimitFileSave(){
        List<CodeActivityDto> activities = new ArrayList<>();
        activities.add(newDto(0L,"a"));
        activities.add(newDto(60*1000L,"a"));
        activities.add(newDto(10*60*1000L,"a"));
        activities.add(newDto(1000L,"b"));
        List<CodeActivityDto> newDtos = codeActivityService.limitFileSaveEvent(activities);
        assertEquals(3,newDtos.size());
        assertEquals((long)0,(long)newDtos.get(0).getTime()- start);
        assertEquals((long)10*60*1000L,(long)newDtos.get(1).getTime()- start);
        assertEquals((long)1000L,(long)newDtos.get(2).getTime()- start);

    }


}
