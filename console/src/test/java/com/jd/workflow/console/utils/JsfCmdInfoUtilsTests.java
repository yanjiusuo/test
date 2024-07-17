package com.jd.workflow.console.utils;

import com.jd.jsf.gd.server.telnet.ServiceInfoTelnetHandler;
import com.jd.workflow.console.service.plugin.jsf.JsfMethodCmdInfo;
import com.jd.workflow.console.utils.dto.ComplexTypeClass;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import junit.framework.TestCase;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;


public class JsfCmdInfoUtilsTests extends TestCase {
    public ComplexTypeClass aaa(ComplexTypeClass complexTypeClass, List<ComplexTypeClass> classes, String str){
        return null;
    }
    public void testParseJsfMethodInfo(){
        ServiceInfoTelnetHandler telnetHandler = new ServiceInfoTelnetHandler();
        String result = telnetHandler.getMethodInfo(JsfCmdInfoUtilsTests.class, "aaa");
        System.out.println("====");
        System.out.println(result);
        JsfMethodCmdInfo method = JsonUtils.parse(result, JsfMethodCmdInfo.class);
        List<BuilderJsonType> jsonTypes = JsfCmdInfoUtils.parseJsfInfoCmdInputParam(method.getParameters());
        System.out.println(JsonUtils.toJSONString(jsonTypes));
    }

    public void testMaybeSubClass(){
        ScopeMap map = new ScopeMap();
        String type = "com.jd.test.Person";
        assertEquals(JsfCmdInfoUtils.maybeSubClass(type,map),type);
        map.put("ComplexClass",new HashMap<>());
        map.put("Person",new HashMap<>());
        map.put("AAA",new HashMap<>());
        type = "com.jd.test.ComplexClass.Person";
        System.out.println(JsfCmdInfoUtils.maybeSubClass(type,map));
        assertEquals(JsfCmdInfoUtils.maybeSubClass(type,map),"com.jd.test.ComplexClass$Person");

        type = "com.jd.AAA.ComplexClass.Person";
        System.out.println(JsfCmdInfoUtils.maybeSubClass(type,map));
        assertEquals(JsfCmdInfoUtils.maybeSubClass(type,map),"com.jd.AAA$ComplexClass$Person");
    }
}
