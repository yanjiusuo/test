package com.jd.workflow.console.service.impl;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class ApiModelServiceImplTests extends BaseTestCase {
    @Test
    public void testParseJavaBean(){
        String content = getResourceContent("classpath:java/test.java");
        ApiModelServiceImpl service = new ApiModelServiceImpl();
        ObjectJsonType result = service.parseJavaBean(content);
        System.out.println("================================");
        System.out.println(JsonUtils.toJSONString(result));
    }
}
