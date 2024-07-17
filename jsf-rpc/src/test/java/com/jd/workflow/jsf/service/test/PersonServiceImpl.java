package com.jd.workflow.jsf.service.test;


import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class PersonServiceImpl implements IPersonService{




    @Override
    public List<? extends Person> simpleDateFormat(SimpleDateFormat format) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> mapType(Map<String, Object> map) {
        Map<String,Object> ret = new HashMap<>();
        ret.put("a",123);
        return ret;
    }




    @Override
    public Map nonGenericMapType() {
        return null;
    }

    @Override
    public ReportMessage comMsg(ReportMessage comMsg) {
        JsonUtils.toJSONString(comMsg);
        return comMsg;
    }

    @Override
    public Person save(Person person) {
        person.setName("fd");
        person.setAge(123);
        return person;
    }

    @Override
    public ComplexTypeClass test(ComplexTypeClass complexTypeClass) {
      log.info("complexClass={}",JsonUtils.toJSONString(complexTypeClass));
        return complexTypeClass;
    }

    @Override
    public SimpleTypeClass simpleType(SimpleTypeClass simpleTypeClass) {
        log.info("simpleTypeClass={}",JsonUtils.toJSONString(simpleTypeClass));
        return simpleTypeClass;
    }

    @Override
    public List<? extends Person> wildcardType(WildtypeEntity<Integer,?> wildtypeEntity) {
        System.out.println(JsonUtils.toJSONString(wildtypeEntity));
        Person person = new Person();
        person.setId(123L);
        return Collections.singletonList(person);
    }

    public static void main(String[] args) {
        Map<String,? super Person> map = new HashMap<>();
        map.put("123",new Person());
        //System.out.println(JSON.toJSON(map, SerializeConfig.getGlobalInstance().));
    }
}
