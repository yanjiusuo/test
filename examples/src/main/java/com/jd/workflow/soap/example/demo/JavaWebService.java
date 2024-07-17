package com.jd.workflow.soap.example.demo;

/*import javax.jws.WebMethod;
import javax.jws.WebService;*/
import javax.xml.ws.Endpoint;

//@WebService
public class JavaWebService {
    //@WebMethod
    public String sayHello(String str1,String str2){
        System.out.println("获取WenService接口方法sayHello");
        String result = "Hello World, "+str1;
        return result;
    }
    public static void main(String[] args) {
        System.out.println("server is running");
        String address="http://localhost:9999/HelloWorld";
        Object implementor =new JavaWebService();
        Endpoint.publish(address, implementor);
    }
}
