package com.jd.workflow.soap.example;

import com.jd.workflow.soap.example.webservice.UserService;
import com.jd.workflow.soap.example.webservice.impl.UserServiceImpl;
//import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

public class WebServicePublisher {
    public static void main(String[] args) {
        System.out.println("web service start");
        UserService implementor = new UserServiceImpl();
        String address = "http://127.0.0.1:8082/userService";
       /* JaxWsServerFactoryBean factoryBean = new JaxWsServerFactoryBean();
        factoryBean.setAddress(address); // 设置暴露地址
        factoryBean.setServiceClass(UserService.class); // 接口类
        factoryBean.setServiceBean(implementor); // 设置实现类
        factoryBean.create();
        System.out.println("web service started");
        System.out.println("web service started");*/


    }
}
