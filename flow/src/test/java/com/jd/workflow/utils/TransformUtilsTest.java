package com.jd.workflow.utils;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.utils.TransformUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.junit.Test;

import java.util.Map;

public class TransformUtilsTest extends BaseTestCase {
    @Test
    public void testJsonPath(){
        String fullTypedTestData  = getResourceContent("classpath:json/FullTypedTestData.json");
        Map<String,Object> map = JsonUtils.parse(fullTypedTestData,Map.class);
        assertEquals(13,TransformUtils.jsonGet(map,"arg1.child.intVar"));
    }
}
