package com.jd.workflow.jsf.service.test;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

public interface IPersonService {
    public Person save(Person person);
    public ComplexTypeClass test(ComplexTypeClass complexTypeClass);
    public SimpleTypeClass simpleType(SimpleTypeClass simpleTypeClass);


    List<? extends Person> wildcardType(WildtypeEntity<Integer,?> wildtypeEntity);
    List<? extends Person> simpleDateFormat(SimpleDateFormat format);
    Map<String,Object> mapType(Map<String,Object> map);
    Map nonGenericMapType();


    public ReportMessage comMsg(ReportMessage comMsg);


}
