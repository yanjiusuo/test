package com.jd.workflow.soap.schema;

import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebService;

import lombok.Data;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

@WebService
public class DemoWebServicePublisher {
    @WebMethod(operationName = "fullTyped")
    public String fullTyped(String str1, String typed){
        System.out.println("获取WenService接口方法sayHello");
        String result = "Hello World, "+str1;

        return result;
    }

    @WebMethod(operationName = "test2")
    public String test2(String str1, String some){
        System.out.println("获取WenService接口方法test2");
        String result = "test2, "+str1+" ,"+some;
        return result;
    }

    @WebMethod(operationName = "exception")
    @WebResult(name = "typed")
    public  void exception(int a) throws UserDefineException,IllegalArgumentException {
        if(a==1){
            throw new UserDefineException();
        }else{
            throw new IllegalArgumentException("123");
        }
    }


    @WebMethod(operationName = "allTyped")
    @WebResult(name = "typed")
    public  SimpleTyped allTyped(SimpleTyped typed)  {
        SimpleTyped ret = new SimpleTyped();
        return ret;
    }

    /**
     *
     * @param
     * @param address "http://127.0.0.1:7001/FullTypedWebService"
     */
    public static void  run(String address){
        System.out.println("web service start");
        DemoWebServicePublisher implementor = new DemoWebServicePublisher();
        JaxWsServerFactoryBean factoryBean = new JaxWsServerFactoryBean();
        factoryBean.setAddress(address); // 设置暴露地址
        factoryBean.setServiceClass(DemoWebServicePublisher.class); // 接口类
        factoryBean.setServiceBean(implementor); // 设置实现类
        factoryBean.create();
        System.out.println("web service started");
        System.out.println("web service started");
    }
    public static void main(String[] args) {
        run("http://localhost:7001/FullTypedWebService");
    }
    @Data
    public static class SimpleTyped{
        int id;
        String name;
    }
}
