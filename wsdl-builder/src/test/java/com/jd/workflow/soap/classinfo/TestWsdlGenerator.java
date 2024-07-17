package com.jd.workflow.soap.classinfo;

import com.jd.workflow.soap.BaseTestCase;
import com.jd.workflow.soap.utils.WsdlUtils;
import com.jd.workflow.soap.wsdl.ServiceMethodInfo;
import com.jd.workflow.soap.wsdl.WsdlGenerator;
import com.jd.workflow.soap.wsdl.WsdlModelInfo;
import com.jd.workflow.soap.wsdl.param.Param;
import com.jd.workflow.soap.wsdl.param.ParamType;
import junit.framework.TestCase;
import org.apache.cxf.BusException;
import org.apache.cxf.service.model.ServiceInfo;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;

public class TestWsdlGenerator extends BaseTestCase {
    static  final Logger logger = LoggerFactory.getLogger(TestWsdlGenerator.class);
    WsdlGenerator wsdlGenerator = null;
    WsdlModelInfo wsdlModelInfo;
    Param newUser(){
        Param user = new Param("user", ParamType.OBJECT);
        user.setName("User");
        user.setChildren(new ArrayList<>());
        user.getChildren().add(new Param("id", ParamType.INTEGER));
        user.getChildren().add(new Param("name", ParamType.STRING));
        return user;
    }
    @Before
    public void setUp() throws Exception {


        wsdlModelInfo = new WsdlModelInfo();
        wsdlModelInfo.setTargetNamespace("http://com.jd.test/");
        wsdlModelInfo.setServiceName("UserService");
        List<ServiceMethodInfo> methods = new ArrayList<>();
        wsdlModelInfo.setMethods(methods);
        ServiceMethodInfo method = new ServiceMethodInfo("update",false);
        method.setInputParams(Collections.singletonList(newUser()));
        method.setOutParam(newUser());
        methods.add(method);

        wsdlGenerator = new WsdlGenerator(wsdlModelInfo);


    }
    @Test
    public void testWsdlGenerator() throws WSDLException, UnsupportedEncodingException, BusException {
        Definition def = wsdlGenerator.buildWsdlDefinition();


        String wsdlContent =   WsdlUtils.wsdlToString(def);

        /*WSDLWriter wsdlWriter = new WSDLManagerImpl().getWSDLFactory().newWSDLWriter();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        wsdlWriter.writeWSDL(def,pw);*/

        logger.info("wsdl_content_is:"+wsdlContent);

        /*Document document = wsdlGenerator.buildServiceInfo(def);
        System.out.println(document.toString());*/
        //System.out.println(serviceInfo);
    }
    @Test
    public void testParseWsdl(){
      /*  File wsdl = new File("d:/tmp/User.xml");
        Definition definition = null;
        WSDLReader reader = null;
        try {
            //validateWsdl(wsdlUrl);
            reader = WSDLFactory.newInstance().newWSDLReader();
            definition = reader.readWSDL("http://172.22.16.1:8000/User.xml");
            System.out.println(definition);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            logger.error("error occur when read wsdl!", e);
        }*/
    }
    @Test
    public void testValidateWsdl() throws IOException {


          /*  URL wsdl = new URL("http://172.22.16.1:8000/User.xml");
            BufferedReader in = new BufferedReader(new InputStreamReader(wsdl.openStream(), Charset.defaultCharset()));

            boolean isWsdl2 = false;
            boolean isWsdl10 = false;
            StringBuilder urlContent = new StringBuilder();
            try {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    String wsdl2NameSpace = "http://www.w3.org/ns/wsdl";
                    String wsdl10NameSpace = "http://schemas.xmlsoap.org/wsdl/";
                    urlContent.append(inputLine);
                    isWsdl2 = urlContent.indexOf(wsdl2NameSpace) > 0;
                    isWsdl10 = urlContent.indexOf(wsdl10NameSpace) > 0;
                }
            } finally {
                in.close();
            }*/
           /* if (isWsdl10) {
                WSDLReader wsdlReader11 = WSDLFactory.newInstance().newWSDLReader();
                wsdlReader11.readWSDL(url);
            }*/


    }
}
