package com.jd.workflow.soap.example;

import org.apache.cxf.tools.common.ToolContext;
import org.apache.cxf.tools.wsdlto.WSDLToJava;

public class WsdlToJavaTest {
    public static void main(String[] args) throws Exception {

           /* System.out.println("web service start");
            UserService implementor = new UserServiceImpl();
            String address = "http://127.0.0.1:8082/userService";
            JaxWsServerFactoryBean factoryBean = new JaxWsServerFactoryBean();
            factoryBean.setAddress(address); // 设置暴露地址
            factoryBean.setServiceClass(UserService.class); // 接口类
            factoryBean.setServiceBean(implementor); // 设置实现类
            factoryBean.create();
            System.out.println("web service started");
            System.out.println("web service started");*/

            /*final WSDLReader reader = new WSDLReaderImpl();
            Definition definition = reader.readWSDL("http://localhost:8000/camel-example-cxf-tomcat/webservices/incident?wsdl");
            System.out.println(definition);*/
        WSDLToJava java = new WSDLToJava();
        String[] arg = new String[]{"-d","d:/tmp/aaa",
            //"http://localhost:8000/camel-example-cxf-tomcat/webservices/incident?wsdl"
                "D:\\tmp\\User.xml"
        };
        ToolContext toolContext = new ToolContext();
        toolContext.setPackageName("com.jjd");
        java.main(arg);
    }
}
