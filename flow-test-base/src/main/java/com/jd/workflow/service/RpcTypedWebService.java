package com.jd.workflow.service;

import com.jd.workflow.entity.FullTyped;
import com.jd.workflow.entity.SimpleTyped;
import lombok.extern.slf4j.Slf4j;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxws.JaxWsServerFactoryBean;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Holder;
import java.util.function.BiFunction;

/**
 * 用来发布一个webService请求，用来做调用测试
 java对rpc/encoded类型的webservice已经不再支持，参考 docs/JAVA的WebService支持.pdf,因此即使指定了use=SOAPBinding.Use.ENCODED也会被忽略
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC,use = SOAPBinding.Use.ENCODED)
@Slf4j
public class RpcTypedWebService {
    BiFunction callback;

    @WebMethod(operationName = "rpcStyle")
    @SOAPBinding(style = SOAPBinding.Style.RPC)
    @WebResult(name = "retValue")
    public  SimpleTyped rpcStyle(int a,SimpleTyped typed){
        SimpleTyped ret = new SimpleTyped();
        ret.setId(typed.getId());
        ret.setName(typed.getName());
        return ret;
    }
    @WebMethod(operationName = "encode")
    @SOAPBinding(style = SOAPBinding.Style.RPC,use = SOAPBinding.Use.ENCODED)
    @WebResult(name = "retValue")
    public  SimpleTyped encode( int a,@WebParam(name = "typed") SimpleTyped typed ){
        SimpleTyped ret = new SimpleTyped();
        ret.setId(typed.getId());
        ret.setName(typed.getName());
        return ret;
    }
    /**
     *
     * @param callback
     * @param address "http://127.0.0.1:7001/FullTypedWebService"
     */
    public static Server  run(BiFunction callback,String address){
        System.out.println("web service start");
        RpcTypedWebService implementor = new RpcTypedWebService();
        implementor.callback = callback;
        JaxWsServerFactoryBean factoryBean = new JaxWsServerFactoryBean();
        factoryBean.setAddress(address); // 设置暴露地址
        factoryBean.setServiceClass(RpcTypedWebService.class); // 接口类
        factoryBean.setServiceBean(implementor); // 设置实现类
        Server server = factoryBean.create();
        System.out.println("web service started");
        System.out.println("web service started");
        return server;
    }
    public static void main(String[] args) {
        run(null,"http://127.0.0.1:5001/FullTypedWebService");
    }
}
