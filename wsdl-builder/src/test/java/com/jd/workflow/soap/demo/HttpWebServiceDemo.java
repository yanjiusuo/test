package com.jd.workflow.soap.demo;

import com.jd.workflow.soap.wsdl.HttpDefinition;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import java.util.List;

/**
 * http转webservice示例
 */
@WebService
public class HttpWebServiceDemo {
 /*   @WebMethod(operationName = "getPerson")
    public GetPersonResponse getPerson(@WebParam(name="body") GetPersonBody person, @WebParam(name="params") GetPersonParams params, @WebParam(name="headers") GetPersonHeaders  headers){
        return new HttpResponse();
    }*/
    @WebMethod(operationName = "executeHttp")
    public HttpResponse executeHttp(@WebParam(name="body") Person person, @WebParam(name="params") List<ParamItem> params,
                                    @WebParam(name="headers") List<ParamItem>  headers){
        return new HttpResponse();
    }

    public static void main(String[] args) {
        String address = "http://127.0.0.1:7002/HttpWebServiceDemo";
        System.out.println("web service start");
        HttpWebServiceDemo implementor = new HttpWebServiceDemo();

        JaxWsServerFactoryBean factoryBean = new JaxWsServerFactoryBean();
        factoryBean.setAddress(address); // 设置暴露地址
        factoryBean.setServiceClass(HttpWebServiceDemo.class); // 接口类
        factoryBean.setServiceBean(implementor); // 设置实现类
        factoryBean.create();
        System.out.println("web service started");
        System.out.println("web service started");
    }
}
