package com.jd.workflow.console.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.jd.workflow.BaseTestCase;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

public class RpcUtilsTests extends BaseTestCase {
    public Long save(List<Long> ids){
        return null;
    }
    @Test
    public void testParse() throws NoSuchMethodException {
        String json = "[[1,2]]";
        ParserConfig parserConfig = new ParserConfig();
        parserConfig.setAutoTypeSupport(true);
        ///ParserConfig.getGlobalInstance().setAutoTypeSupport(true);
        parserConfig.addAccept("com.jd");
        parserConfig.addAccept("com.jdh");
        parserConfig.addAccept("com.jdl");
        Method method = RpcUtilsTests.class.getMethod("save",List.class);
        Object obj = JSON.parseArray(json,method.getParameterTypes(),parserConfig);
        Object obj1 = JSON.parseArray(json,method.getGenericParameterTypes(),parserConfig);
        System.out.println(obj);
    }
}
