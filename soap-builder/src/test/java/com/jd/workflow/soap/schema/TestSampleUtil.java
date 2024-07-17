package com.jd.workflow.soap.schema;

import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.legacy.SampleXmlUtil;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import com.jd.workflow.soap.legacy.WsdlUtils;
import junit.framework.TestCase;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapOperation;
import org.springframework.util.ResourceUtils;
import org.w3c.dom.Element;

import javax.wsdl.*;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.URL;
import java.util.*;

public class TestSampleUtil   extends TestCase {
    String wsdlServiceUrl = "http://localhost:8010/demo";
    String wsdlUrl = wsdlServiceUrl+"?wsdl";
    SampleXmlUtil sampleXmlUtil;
    SoapMessageBuilder soapMessageBuilder;
    @Override
    public void setUp() throws Exception {
        super.setUp();
        DemoWebServicePublisher.run(wsdlServiceUrl);

        sampleXmlUtil = new SampleXmlUtil(true, SoapContext.builder().build());
        soapMessageBuilder = new SoapMessageBuilder(new URL(wsdlUrl));
    }

    /**
     * 解析wsdl url信息
     */
    public Definition readWsdlUrl() throws WSDLException {
        return readWsdlUrl(wsdlUrl.toString());
    }
    /**
     * 解析wsdl url信息
     */
    public Definition readWsdlUrl(String wsdlUrl) throws WSDLException {
        WSDLReader reader = new WSDLReaderImpl();
        reader.setFeature("javax.wsdl.verbose", false);
        Definition definition = reader.readWSDL(wsdlUrl.toString());
        return definition;
    }
    /**



     * @throws Exception
     */
    public void testCreateInputMsg() throws Exception {
        SoapContext context = SoapContext.DEFAULT;
        Definition definition = readWsdlUrl("http://111.17.194.18:9002/his/Service.asmx?wsdl");
        for (Object o : definition.getAllBindings().entrySet()) {
            Map.Entry<QName, Binding> entry = (Map.Entry<QName, Binding>) o;
            Binding binding = entry.getValue();


            List<BindingOperation> operations = binding.getBindingOperations();
            for (BindingOperation bindingOperation : operations) {
                SOAPOperation soapOperation = (SoapOperation) WsdlUtils.getExtensiblityElement(bindingOperation.getExtensibilityElements(), SOAPOperation.class);
                final Operation operation = bindingOperation.getOperation();
                final Element operationDoc = operation.getDocumentationElement();
                if(operationDoc!=null && operationDoc.getFirstChild() != null
                ){
                    String desc = operationDoc.getFirstChild().getNodeValue();
                }
                operationDoc.getFirstChild().getNodeValue();
                operation.getExtensibilityElements();
                System.out.println(operation);
              /* String inputMsg =  soapMessageBuilder.buildSoapMessageFromInput(entry.getValue(),bindingOperation,context); // 构造示例输入数据
               String outMsg =  soapMessageBuilder.buildSoapMessageFromOutput(entry.getValue(),bindingOperation,context); // 构造示例输出数据
                System.out.println("inputMsg="+inputMsg);
                System.out.println("outMsg="+outMsg);*/
            }

        }

    }

}
