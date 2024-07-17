package com.jd.workflow.soap.example.webservice.impl;

import com.jd.workflow.soap.example.webservice.UserService;
import com.jd.workflow.soap.example.webservice.model.User;


import javax.xml.ws.Endpoint;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class UserServiceImpl  implements UserService {

    @Override
    public Map<String, Object> getUserInfo(String userId, String name) {
        return null;
    }

    //@WebMethod
    public void save(User user) {

    }

    //@WebMethod
    public int update(User user) {
        return 0;
    }

    //@WebMethod
    public String[] testArray(String[] ids, Integer names) {
        return new String[0];
    }
    //@WebMethod
    public List<User> testArray(String[] ids) {
        return null;
    }

    //@WebMethod
    public float testFloat(float param) {
        return 0;
    }
    //@WebMethod
    public Date testDate(long timestamp) {
        return new Date();
    }


    /*public Timestamp testTimestamp(long timestamp) {
        return new Timestamp(timestamp);
    }*/
    ////@WebMethod
    public static void main(String[] args) {
        System.out.println("server is running");
        String address="http://localhost:9999/users";
        Object implementor =new  UserServiceImpl();
        Endpoint.publish(address, implementor);
    }
  

}
