package com.jd.workflow.jsf.api;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.jsf.metadata.JsfStepMetadata;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.parser.ClassParser;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.core.DefaultParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.*;

@Slf4j
public class ClassParserTests extends BaseTestCase {

    @Test
    public void testParse(){
        ClassParser handler = new ClassParser();
        MethodMetadata methodInfo = handler.buildMethodInfo(ClassParserTests.class, "testComplexMethod");
        log.info("methodInfo={}", JsonUtils.toJSONString(methodInfo));
    }

    /**
     * fsddsf
     * @param type
     */
    public void testComplexMethod(ComplexTypeClass type){

    }
    @Data
    public static class Pair<K,V>{
        K key;
        V value;
        K[] genericArrays;
        List<K>[] genericArrayList;
        List<List<K>> genericArrayArrayList;
    }
    @Data
    public static class ComplexTypeClass{
        String strVar;
       /*int intVar;
        short shortVar;
        long longVar;
        float floatVar;
        double doubleVar;
        char charVar;
        //@XmlElement(required = true)
        boolean booleanVar;
        String strVar;
        List<?> wildcardList;
        Pair<String,Object> pairObj;
        Pair<? extends Number,? super Integer> wildPair;
        BigInteger bigIntegerVar;
        BigDecimal bigDecimalVar;
        byte byteVar;
        QName qNameVar;
        XMLGregorianCalendar dateTimeVar;
        Timestamp timestamp;
        Date date;
        java.sql.Date sqlDate;
        byte[] bytesVar;
        int[] intsVars;*/
       /* List<Integer> intList;
        Integer[] intArr;*/
     /*    Duration durationVar;
        ArrayList<FullTyped.Child> childList;
     HashMap<String, String> childHashMap;
       HashMap dd;

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
         ComplexTypeClass typeChild;
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



        }*/


    }

    public static void main(String[] args) {

        DefaultParameterNameDiscoverer discoverer = new DefaultParameterNameDiscoverer();
        Map<String,Object> map = new HashMap<>();
        for (Method method : ClassParserTests.class.getMethods()) {
            if("testComplexMethod".equals(method.getName())){
                final String[] parameterNames = discoverer.getParameterNames(method);
                System.out.println(parameterNames);
            }
            System.out.println(method);
        }
    }
}
