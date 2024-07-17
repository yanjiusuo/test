package com.jd.workflow.soap.example.jsf.entity;

import lombok.Data;

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
