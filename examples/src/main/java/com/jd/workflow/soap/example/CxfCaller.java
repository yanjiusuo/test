package com.jd.workflow.soap.example;


import org.apache.cxf.common.util.StringUtils;
import org.apache.cxf.endpoint.Client;
//import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.staxutils.StaxUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jws.WebMethod;
import javax.wsdl.*;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.ws.Service;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

public class CxfCaller {
    static final Logger LOG = LoggerFactory.getLogger(CxfCaller.class);
    public static void validateWsdl(String url) throws IOException, WSDLException {
        if (StringUtils.isEmpty(url)) {
            throw new RuntimeException("URL is not empty");
        }
        URL wsdl = new URL(url);
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
        }
        if (isWsdl10) {
            WSDLReader wsdlReader11 = WSDLFactory.newInstance().newWSDLReader();
            wsdlReader11.readWSDL(url);
        }
		    /*else if (isWsdl2)
		    {
		      org.apache.woden.WSDLReader wsdlReader20 = org.apache.woden.WSDLFactory.newInstance().newWSDLReader();
		      wsdlReader20.readWSDL(url);
		    }*/
        else {
            throw new RuntimeException("soa.CAN_err_invalid_wsdl_content");
        }
    }

   /* public static void buildXml(){
        Wsdl wsdl = Wsdl.parse("http://localhost:8000/camel-example-cxf-tomcat/webservices/incident?wsdl");

        SoapBuilder builder = wsdl.binding()
                .localPart("IncidentServiceSoapBinding")
                .find();
        SoapOperation operation = builder.operation()
                .name("statusIncident")
                .find();

        String request = builder.buildInputMessage(operation);
        System.out.println(request);
        *//*SoapClient client = SoapClient.builder()
                .endpointUrl("http://www.webservicex.net/CurrencyConvertor.asmx")
                .build();
        String response = client.post(request);*//*
    }*/

    public static Definition readWsdl(String wsdlUrl){
        Definition definition = null;
        WSDLReader reader = null;
        try {
            validateWsdl(wsdlUrl);
            reader = WSDLFactory.newInstance().newWSDLReader();
            definition = reader.readWSDL(wsdlUrl);
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            LOG.error("error occur when read wsdl!", e);
            return null;
        }

      /*  Client client = null;
        try {
            JaxWsDynamicClientFactory factory = JaxWsDynamicClientFactory.newInstance();
            client = factory.createClient(wsdlUrl);
        } catch (Exception e) {
            LOG.error("error occur when read wsdl!", e);
            return null;
        }

        ServiceInfo info = client.getEndpoint().getEndpointInfo().getService();*/
        // Map portTypes =  definition.getPortTypes();
        return definition;
    }

    public void transformToXml(String wsdl,String operation,String jsonInput){
        Definition definition = readWsdl(wsdl);
        String targetNameSpace = definition.getTargetNamespace();
        for (Object o : definition.getBindings().entrySet()) {
            Map.Entry<QName,Binding> bindingEntry = (Map.Entry<QName, Binding>) o;
            for (Object o1 : bindingEntry.getValue().getBindingOperations()) {
                BindingOperation bindingOperation = (BindingOperation) o1;
                if(operation.equals(bindingOperation.getName())){ // 对应的操作名称
                    Operation operationInfo = bindingOperation.getOperation();

                }
                bindingOperation.getName();
            }
        }
    }

    public static void main(String[] args) throws MalformedURLException {
        String req = "<hel:test xmlns:hel=\"http://helpconsole.jd.local\"/>";
        String url = "http://11.120.32.177/demo/api?wsdl";
        URL wsdlURL = new URL(url);

        Service service = Service.create(wsdlURL, new QName("http://helpconsole.jd.local","DemoServiceImplService"));
        Dispatch<Source> disp = service.createDispatch(new QName("http://helpconsole.jd.local","DemoServicePort"), Source.class, Service.Mode.PAYLOAD);

        Source request = new StreamSource(new StringReader(req));

        Source response = disp.invoke(request);
        String result =  StaxUtils.toString(response);
        System.out.println("response::"+result);

    }
    private void test() throws Exception {
        /*JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
        Client client = dcf.createClient("echo.wsdl");

        Object[] res = client.invoke("echo", "test echo");
        System.out.println("Echo response: " + res[0]);*/
    }
}
