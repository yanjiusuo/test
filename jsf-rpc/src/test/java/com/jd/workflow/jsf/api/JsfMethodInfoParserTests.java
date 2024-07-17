package com.jd.workflow.jsf.api;

import com.jd.jsf.gd.server.telnet.ServiceInfoTelnetHandler;
import com.jd.workflow.BaseTestCase;
import com.jd.workflow.entity.FullTyped;
import com.jd.workflow.jsf.service.test.ComplexTypeClass;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Slf4j
public class JsfMethodInfoParserTests extends BaseTestCase {

    @Test
    public void testParse(){
        ServiceInfoTelnetHandler handler = new ServiceInfoTelnetHandler();
        String methodInfo = handler.getMethodInfo(JsfMethodInfoParserTests.class, "testComplexMethod");
        log.info("methodInfo={}",methodInfo);
    }
    public void testComplexMethod(ComplexTypeClass type, Pair<String,Object> pair){

    }
    @Data
    public static class Pair<K,V>{
        K key;
        V value;
    }

}
