package com.jd.workflow.soap.example.jsf.entity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public interface IPersonService {
    public Person save(Person person);

    public ComplexTypeClass complexType(ComplexTypeClass complexTypeClass);
    public SimpleTypeClass simpleType(SimpleTypeClass simpleTypeClass);


    WildtypeEntity<Integer,?>  wildcardType(WildtypeEntity<Integer,?> wildtypeEntity);
    List<? extends Person> simpleDateFormat(SimpleDateFormat format);
    Map<String,Object> mapType(Map<String,Object> map);

    public void noOutput(int a,int b);
}
