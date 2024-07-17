package com.jd.workflow.jsf.service.test;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class WildtypeEntity<E extends Number,B> {
   /* List<? extends Person> personList;
    List<? super Man> man;
    List<? extends List<?>> listList;
    List<?> list;
    Map<? extends Number,? super Person> map;*/
  /*  E genericType;
    B b;
    List<E> list;
    Map<String,E> map;*/
    Pair<E,B> pair;
    @Data
    static class Pair<K,V>{
        K key;
        V value;
        Pair<String,K> child;
    }

    @Data
    static class Person{
        String name;
    }
    @Data
    static class Man extends Person{
        String age;
    }
    @Data
    static class Woman extends Person{
        String age;
    }
}
