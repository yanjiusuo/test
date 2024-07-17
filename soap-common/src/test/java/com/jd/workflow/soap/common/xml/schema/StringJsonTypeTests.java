package com.jd.workflow.soap.common.xml.schema;

import com.jd.workflow.soap.common.BaseTestCase;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;

/**
 测试JsonType处理字符串json以及字符串xml是否存在问题
 */

public class StringJsonTypeTests extends BaseTestCase {
    @Test
    public void testJsonString(){
        String str = getResourceContent("classpath:json_type/json-string-or-array.json");
        JsonType jsonType = JsonUtils.parse(str,JsonType.class);

    }
}
