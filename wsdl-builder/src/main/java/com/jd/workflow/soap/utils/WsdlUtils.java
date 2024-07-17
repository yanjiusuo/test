package com.jd.workflow.soap.utils;

import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.exception.WsdlGenerateException;
import org.apache.commons.io.IOUtils;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.soap.wsdl.extensions.SoapAddress;
import org.apache.cxf.wsdl11.WSDLManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

public class WsdlUtils {
    static final Logger logger = LoggerFactory.getLogger(WsdlUtils.class);
    public static String wsdlToString(Definition def){
        WSDLWriter wsdlWriter = null;
        try {
            wsdlWriter = new WSDLManagerImpl().getWSDLFactory().newWSDLWriter();
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            wsdlWriter.writeWSDL(def,pw);
            return sw.toString();
        } catch (Exception e) {
            logger.error("wsdl.err_generate_wsdl",e);
            throw new WsdlGenerateException("wsdl.err_generate_wsdl",e);
        }

    }
    private static Document getDocument(InputSource inputSource,
                                        String desc) throws WSDLException
    {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);
        factory.setValidating(false);

        try
        {
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(inputSource);

            return doc;
        }
        catch (RuntimeException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new WSDLException(WSDLException.PARSER_ERROR,
                    "Problem parsing '" + desc + "'.",
                    e);
        }
    }

    public static Definition parseWsdl(String wsdlUri){
        Definition definition = null;

        try {
            WSDLReader reader =  WSDLFactory.newInstance().newWSDLReader();;
            definition = reader.readWSDL(wsdlUri);
            return definition;
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            logger.error("wsdl.err_parse_wsdl_url",e);
            throw new StdException("wsdl.err_parse_wsdl",e);
        }
    }
    public static Definition parseWsdlByContent(String wsdlString){
        Definition definition = null;
        WSDLReader reader = null;
        try {
            InputStream inputStream = IOUtils.toInputStream(wsdlString);
            InputSource inputSource = new InputSource(inputStream);
            Document doc = getDocument(inputSource, "wsdl");
            //validateWsdl(wsdlUrl);
            reader = WSDLFactory.newInstance().newWSDLReader();
            definition = reader.readWSDL(null,doc);
            return definition;
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            logger.error("wsdl.err_parse_wsdl_url",e);
            throw new StdException("wsdl.err_parse_wsdl",e);
        }
    }

    public static String replaceServiceAddressByUri(String wsdlUri,String endpointUrl){
        Definition definition = parseWsdl(wsdlUri);
        for (Object o : definition.getServices().entrySet()) {
            Map.Entry<QName, Service> entry = (Map.Entry<QName, Service>) o;
            for (Object o1 : entry.getValue().getPorts().entrySet()) {
                Map.Entry<String, Port> portEntry = (Map.Entry<String, Port>) o1;
                for (Object extensibilityElement : portEntry.getValue().getExtensibilityElements()) {
                    SOAPAddressImpl address = (SOAPAddressImpl) extensibilityElement;
                    address.setLocationURI(endpointUrl);
                }
            }
        }
        return wsdlToString(definition);
    }

    /**
     * 将服务端地址替换掉
     * @param wsdlString
     * @param endpointUrl
     * @return
     */
    public static String replaceServiceAddressByContent(String wsdlString, String endpointUrl){
        Definition definition = parseWsdlByContent(wsdlString);
        for (Object o : definition.getServices().entrySet()) {
            Map.Entry<QName, Service> entry = (Map.Entry<QName, Service>) o;
            System.out.println(entry);
            for (Object o1 : entry.getValue().getPorts().entrySet()) {
                Map.Entry<String, Port> portEntry = (Map.Entry<String, Port>) o1;
                for (Object extensibilityElement : portEntry.getValue().getExtensibilityElements()) {
                    SOAPAddressImpl address = (SOAPAddressImpl) extensibilityElement;
                    address.setLocationURI(endpointUrl);
                }
            }
        }
        return wsdlToString(definition);
    }


    public static void main(String[] args) {
        String result = replaceServiceAddressByUri("http://127.0.0.1:7001/FullTypedWebService?wsdl","123");
        System.out.println(result);
    }
}
