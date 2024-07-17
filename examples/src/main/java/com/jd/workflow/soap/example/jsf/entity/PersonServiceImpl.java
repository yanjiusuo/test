package com.jd.workflow.soap.example.jsf.entity;


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

        return map;
    }

    @Override
    public void noOutput(int a, int b) {
        System.out.println("a:"+a+" b:"+b);
    }


    @Override
    public Person save(Person person) {
        return person;
    }

    @Override
    public ComplexTypeClass complexType(ComplexTypeClass complexTypeClass) {
      log.info("complexClass={}",JsonUtils.toJSONString(complexTypeClass));
        return complexTypeClass;
    }

    @Override
    public SimpleTypeClass simpleType(SimpleTypeClass simpleTypeClass) {
        log.info("simpleTypeClass={}",JsonUtils.toJSONString(simpleTypeClass));
        return simpleTypeClass;
    }

    @Override
    public WildtypeEntity<Integer, ?> wildcardType(WildtypeEntity<Integer, ?> wildtypeEntity) {
        return wildtypeEntity;
    }


    public static void main(String[] args) {
        Map<String,? super Person> map = new HashMap<>();
        map.put("123",new Person());
        //System.out.println(JSON.toJSON(map, SerializeConfig.getGlobalInstance().));
    }
}
