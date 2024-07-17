package com.jd.workflow.console.utils.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jd.jsf.gd.server.telnet.ServiceInfoTelnetHandler;
import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

@Data
public  class ComplexTypeClass {
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
  //  QName qNameVar;
    //XMLGregorianCalendar dateTimeVar;
    Timestamp timestamp;
    Date date;
    java.sql.Date sqlDate;
    byte[] bytesVar;
    private Page<Person> pagePerson;
    private CommonResult<Person> result;
    private List<Parent> parentList;
    ArrayList<Child> childList;
    HashMap<String, Child> childHashMap;
    Map<?,Person> wildTypeMap;
    ArrayList<String> stringList;
    HashMap<String,String> stringHashMap;
    String[] strArray;
    String[][] strArrArr;

    // @XmlElement(required = true)
    Child[] children;
    //@XmlElement(required = true)
    Child[][] childrenChildren;
    Child child;
    ComplexTypeClass typeChild;

    Person person;

    char[] charArray;


    Time time;

    URL url;
    URI uri;
    Class clazz;
    Calendar calendar;
    Locale locale;
    TimeZone timeZone;
    UUID uuid;
    Fruit fruit;
    InterfaceTypeEnum interfaceTypeEnum;

    public static  enum Fruit{
        apple,orange
    }

    public enum InterfaceTypeEnum {
        HTTP(1,"http"),
        WEB_SERVICE(2,"webservice"),
        JSF(3,"jsf"),
        //编排
        ORCHESTRATION(10,"orchestration"),
        ;

        /**
         * @date: 2022/5/12 18:23
         * @author wubaizhao1
         */
        @Getter
        @Setter
        private Integer code;

        /**
         * 描述
         * @date: 2022/5/12 18:25
         * @author wubaizhao1
         */
        @Getter
        @Setter
        private String desc;

         InterfaceTypeEnum(Integer code, String desc) {
            this.code = code;
            this.desc = desc;
        }
    }



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



    }
    @Data
    public static class EnumTest{
        InterfaceTypeEnum interfaceTypeEnum;
        Fruit fruit;
    }
    public ComplexTypeClass test(ComplexTypeClass complexTypeClass,List<ComplexTypeClass> classes,String str){
        return null;
    }
    public static  void main(String[] args){
        EnumTest test = new EnumTest();
        test.interfaceTypeEnum = InterfaceTypeEnum.HTTP;
        test.fruit = Fruit.apple;
        final String s = JsonUtils.toJSONString(test);
        System.out.println(s);
        ServiceInfoTelnetHandler telnetHandler = new ServiceInfoTelnetHandler();
        String result = telnetHandler.getMethodInfo(ComplexTypeClass.class, "test");
        System.out.println("====");
        System.out.println(result);
    }
}