package com.jd.workflow;

import com.ibm.wsdl.xml.WSDLReaderImpl;
import com.jd.workflow.service.FullTypedWebService;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.legacy.SampleXmlUtil;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import org.apache.cxf.endpoint.Server;
import org.apache.xmlbeans.SchemaGlobalElement;
import org.apache.xmlbeans.SchemaType;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.net.URL;

public class WebServiceBaseTestCase extends BaseTestCase{
    public static  String WEBSERVICE_URL = "http://127.0.0.1:7001/FullTypedWebService";

   public static String wsdlUrl = WEBSERVICE_URL+"?wsdl";
    public static SoapMessageBuilder soapMessageBuilder;
    public static SampleXmlUtil sampleXmlUtil = null;
    public static Server server = null;

    public Definition readWsdlUrl() throws WSDLException {
         return readWsdlUrl(wsdlUrl);
    }
    public Definition readWsdlUrl(String wsdlUrl) throws WSDLException {
        WSDLReader reader = new WSDLReaderImpl();
        reader.setFeature("javax.wsdl.verbose", false);
        return reader.readWSDL(wsdlUrl.toString());
    }
    public SchemaType loadSchemaType(QName name){
        SchemaGlobalElement elm =  soapMessageBuilder.getSchemaDefinitionWrapper().getSchemaTypeLoader().findElement(name);
        return elm.getType();
    }

    @BeforeClass
    public static void beforeClass() throws Exception {

         server = FullTypedWebService.run(null, WEBSERVICE_URL);
        soapMessageBuilder = new SoapMessageBuilder(new URL(wsdlUrl));
        sampleXmlUtil = new SampleXmlUtil(false, SoapContext.DEFAULT);
    }
    @AfterClass
    public static void afterClass() throws Exception {
        server.stop();
    }
}
