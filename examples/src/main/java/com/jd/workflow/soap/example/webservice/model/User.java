package com.jd.workflow.soap.example.webservice.model;

import lombok.Data;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;

@Data
public class User {
    Integer id;
    String name;
    List<String> friends;
    String[] exts;

    public static void main(String[] args) {
        for (Method method : User.class.getMethods()) {
            Type[] genParTypes =  method.getGenericParameterTypes();
            System.out.println(genParTypes);
        }
    }
}
