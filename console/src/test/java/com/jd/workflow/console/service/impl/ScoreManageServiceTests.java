package com.jd.workflow.console.service.impl;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class ScoreManageServiceTests extends BaseTestCase {
    ScoreManageService scoreManageService = new ScoreManageService();
    @Test
    public void testComputeScore(){
        String content = getResourceContent("classpath:score/score.json");
        List<JsonType> jsonTypes = JsonUtils.parseArray(content, JsonType.class);
        double percent = scoreManageService.getPercent(jsonTypes);
        assertEquals(0.75,percent);
    }
    @Test
    public void testBuildContent(){

    }


}
