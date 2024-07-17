package com.jd.workflow.service;

import com.jd.workflow.entity.FullTyped;
import com.jd.workflow.entity.SimpleTyped;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.bus.extension.ExtensionManagerBus;

import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Holder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 用来发布一个webService请求，用来做调用测试
 */
@WebService
@Slf4j
public class FullTypedWebService {
    BiFunction callback;
    @WebMethod(operationName = "test")
    //@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public String test(String str1, FullTyped typed){
        System.out.println("获取WenService接口方法sayHello");
        String result = "Hello World, "+str1;
        if(callback!=null){
            callback.apply(str1,typed);
        }
        return result;
    }
    @WebMethod(operationName = "simple")
    //@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public String simple(String str1, SimpleTyped typed){
        System.out.println("获取WenService接口方法sayHello");
        String result = "Hello World, "+str1;
        if(callback!=null){
            callback.apply(str1,typed);
        }
        return result;
    }
    @WebMethod(operationName = "sumAndMultiply")
    public void sumAndMultiply(int a, int b,
                               @WebParam(name = "sum", mode = WebParam.Mode.OUT) Holder<Integer> sum,
                               @WebParam(name = "multiply", mode = WebParam.Mode.OUT) Holder<Integer> multiply) {
        sum.value = a + b;
        multiply.value = a * b;
    }
    @WebMethod(operationName = "returnObject")
    @WebResult(name = "typed")
    public  SimpleTyped returnObject(int a, int b) {
         SimpleTyped typed = new SimpleTyped();
         typed.setName("123");
         typed.setId(312L);
         return typed;
    }
    @WebMethod(operationName = "noOutput")
    @WebResult(name = "retValue")
    public  void noOutput(int a) {
        log.info("aaaaa::::{}",a);
    }

    @WebMethod(operationName = "echo")
    @WebResult(name = "output")

    public  String echo( @WebParam(name="input")  String result  ) {
        return result;
    }

    @WebMethod(operationName = "hasHeader")
    @WebResult(name = "retValue")
    public  SimpleTyped hasHeader(
                    @WebParam(header = true,name="a1") int a1,
                    @WebParam(header = true,name="a2") int a2,
                    @WebParam(header = true,name="a3") SimpleTyped a3,
                                  @WebParam(name = "typed")
                                  SimpleTyped typed,
                                  @WebParam(header = true,name="b1",mode= WebParam.Mode.OUT) Holder<Integer> b1
    ){
        SimpleTyped ret = new SimpleTyped();
        ret.setId(typed.getId());
        ret.setName(typed.getName());
        b1.value = a1;
        return ret;
    }

    @WebMethod(operationName = "queryRole")
    public RoleInfo queryRole(@WebParam(name="roleName") String roleName){
        RoleInfo roleInfo = new RoleInfo();
        roleInfo.setId(1L);
        roleInfo.setRoleName(roleName);
        roleInfo.setRoleDesc("管理员");
        roleInfo.setLevel("1");
        roleInfo.setCreateBy("admin");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        roleInfo.setCreateDate("2022-06-29");
        return roleInfo;
    }
    @WebMethod(operationName = "noWrappedArg")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    @WebResult(name = "retValue")
    public  SimpleTyped noWrappedArg(@WebParam(name = "a") int a,@WebParam(name = "typed") SimpleTyped typed){
        SimpleTyped ret = new SimpleTyped();
        ret.setId(typed.getId());
        ret.setName(typed.getName());
        return ret;
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

    @WebMethod(operationName = "noThrowException")
    @WebResult(name = "typed")
    public  void noThrowException(int a)  {
        if(a==1){
            throw new UserDefineException();
        }else{
            throw new IllegalArgumentException("123");
        }
    }
    /**
     *
     * @param callback
     * @param address "http://127.0.0.1:7001/FullTypedWebService"
     */
    public static Server  run(BiFunction callback,String address){
        System.out.println("web service start");
        FullTypedWebService implementor = new FullTypedWebService();
        implementor.callback = callback;
        JaxWsServerFactoryBean factoryBean = new JaxWsServerFactoryBean();
        factoryBean.setAddress(address); // 设置暴露地址
        factoryBean.setServiceClass(FullTypedWebService.class); // 接口类
        factoryBean.setServiceBean(implementor); // 设置实现类
        Server server = factoryBean.create();
        System.out.println("web service started");
        System.out.println("web service started");
        return server;
    }
    @Data
    public static class RoleInfo {
        Long id;
        String roleName;
        String roleDesc;
        String level;
        String createBy;
        String createDate;
    }

    public static void main(String[] args) {
        run(null,"http://127.0.0.1:7001/FullTypedWebService");
    }
}
