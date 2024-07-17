package com.jd.workflow.console.service.remote;

import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jd.cjg.bus.request.InterfaceCreateReq;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.console.service.doc.app.dto.Person;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MockDataBuildServiceTests extends BaseTestCase {
    MockDataBuildService mockDataBuildService = new MockDataBuildService();
    @Test
    public void testBuildJsonData(){
        String contentArr = getResourceContent("classpath:jsf/jsf-mock-data-mock.json");
        List<JsonType> jsonTypes = JsonUtils.parseArray(contentArr, JsonType.class);
        for (JsonType jsonType : jsonTypes) {
            Object result = mockDataBuildService.buildJarJsfCallEmptyValue(jsonType);
            System.out.println(JsonUtils.toJSONString(result));
        }

    }
    @Test
    public void tesetJsonSerialize(){
        Person req = new Person();
        req.setName("123");
        Set<String> strs = new HashSet<>();
        strs.add("123");
        req.setStrs(new HashSet<>());
        req.setObj(new Person());
        String jsonString = com.alibaba.fastjson.JSON.toJSONString(req, SerializerFeature.WriteClassName);

        System.out.println(jsonString);


    }
}
