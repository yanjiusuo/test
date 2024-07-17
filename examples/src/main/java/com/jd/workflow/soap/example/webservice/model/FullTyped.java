package com.jd.workflow.soap.example.webservice.model;


import lombok.Data;

import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 参考这个映射：https://docs.oracle.com/javase/tutorial/jaxb/intro/bind.html
 * 用来做全类型映射，确保解析的对象能够正常被处理
 */
@Data
public class FullTyped {
    int intVar;
    short shortVar;
    long longVar;
    float floatVar;
    double doubleVar;
    char charVar;
    //@XmlElement(required = true)
    boolean booleanVar;
    String strVar;
    BigInteger bigIntegerVar;
    BigDecimal bigDecimalVar;
    byte byteVar;
    QName qNameVar;
    XMLGregorianCalendar dateTimeVar;
    byte[] bytesVar;
    Duration durationVar;
    ArrayList<Child> childList;
    HashMap<String,Child> childHashMap;
    ArrayList<String> stringList;
    HashMap<String,String> stringHashMap;
    String[] strArray;
    String[][] strArrArr;
    String[][][] strArrArrArr;
    String[][][][] strArrArrArrArr;
   // @XmlElement(required = true)
    Child[] children;
    //@XmlElement(required = true)
    Child[][] childrenChildren;
    Child child;


@Data
    public static  class Child {
        int intVar;
        short shortVar;
        long longVar;
        float floatVar;
        double doubleVar;
        char charVar;
        boolean booleanVar;
        String strVar;

        SubChild subChild;




    }
    @Data
    public static class SubChild {
        int intVar;
        /*short shortVar;
        long longVar;
        float floatVar;
        double doubleVar;
        char charVar;
        boolean booleanVar;
        String strVar;*/


    }

}
