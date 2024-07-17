package com.jd.workflow.console.utils;

import com.jd.workflow.BaseTestCase;
import org.junit.Test;

public class ReqDemoBuildUtilsTests extends BaseTestCase {
    @Test
    public void testArray(){
        String content = getResourceContent("classpath:json_schema/test.json");
        com.alibaba.fastjson.JSONObject contentObj = com.alibaba.fastjson.JSON.parseObject(content);
        String result = ReqDemoBuildUtils.getObjectString(contentObj,false);
        System.out.println(result);
    }
}
