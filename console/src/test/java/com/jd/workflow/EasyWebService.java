package com.jd.workflow;

import lombok.Data;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

import javax.jws.WebMethod;
import javax.jws.WebService;
import java.util.function.BiFunction;

@WebService
public class EasyWebService {
    @WebMethod(operationName = "simple")
    //@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public String simple(String str1, SimpleTyped typed){
        System.out.println("获取WenService接口方法sayHello");
        String result = "Hello World, "+str1;

        return result;
    }

    public static void  run(  String address){
        System.out.println("web service start");
        EasyWebService implementor = new EasyWebService();
        JaxWsServerFactoryBean factoryBean = new JaxWsServerFactoryBean();
        factoryBean.setAddress(address); // 设置暴露地址

        factoryBean.setServiceClass(EasyWebService.class); // 接口类
        factoryBean.setServiceBean(implementor); // 设置实现类
        factoryBean.create();
        System.out.println("web service started");
        System.out.println("web service started");
    }
    public static void main(String[] args) {
        run("http://0.0.0.0:3001/FullTypedWebService");
    }
    @Data
    public static class SimpleTyped{
        Long id;
        String name;
    }
}
