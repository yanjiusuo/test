package com.jd.workflow.soap.common.util;

import lombok.Data;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

public class BeanToolTests {
    @Test
    public void testToMap(){
        ComplexData data = new ComplexData();
        data.setIntVar(1);
        short s = 2;
        data.setShortVar(s);
        data.setFloatVar(1);
        ComplexData.Child child = new ComplexData.Child();
        child.setIntVar(1);
        data.getChildList().add(child);
        data.setChild(child);
        data.getChildArr()[0] = child;
        final Map map = BeanTool.toMap(data);
        System.out.println(JsonUtils.toJSONString(map));
    }
    @Data
    public static class ComplexData{
        int intVar;
        short shortVar;
        long longVar;
        float floatVar;
        double doubleVar;
        char charVar;
        //@XmlElement(required = true)
        boolean booleanVar;
        String strVar;

        byte byteVar;


        byte[] bytesVar;

        Child child;
        List<Child> childList = new ArrayList<>();
        Child[] childArr = new Child[1];
        HashMap<String, Child> childHashMap;
        ArrayList<String> stringList;
        HashMap<String,String> stringHashMap;
        String[] strArray;
        String[][] strArrArr;
        @Data
        public static class Child{
            int intVar;
            short shortVar;
        }
    }
}
