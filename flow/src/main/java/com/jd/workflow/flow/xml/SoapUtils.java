package com.jd.workflow.flow.xml;


import com.jd.workflow.flow.core.exception.WebServiceError;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

/**
 *
 *  构造soap报文,主要功能：将soap报文结构构造成前端需要录入的格式

  soap报文后端存储的结构为：
 {

 }


 */
public class SoapUtils {
  static final Logger logger = LoggerFactory.getLogger(SoapUtils.class);
 /*public static JSONObject parseSoapResponse(String soapString){

        SOAPMessage msgs =  formatSoapString(soapString);
        SOAPBody body = null;
        try {
            SOAPEnvelope envelope = msgs.getSOAPPart().getEnvelope();
            body = envelope.getBody();
        } catch (SOAPException e) {
            logger.error("soap.err_parse_response",e);
            throw new StdException("soap.err_parse_response",e);
        }
        Iterator itr= body.getChildElements();

        JSONObject json = new JSONObject();
        getData(itr,json);
        return json;
    }
    //iterator 递归转json
    private static void getData(Iterator<Node> iterator, JSONObject json) {
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o != null) {
                SOAPElement element = null;
                try {
                    element = (SOAPElement) o;
                    json.put(element.getNodeName(),element.getValue());
                } catch (Exception e) {
                    logger.error("soap.err_get_data",e);
                }
                if (element != null) {
                    getData(element.getChildElements(),json);
                }
            }
        }
    }*/

    /**
     * 将xml字符串转换为json对象
     * @param soapXmlString
     * @param jsonType
     * @return
     */
    public static Object soapXmlToJson(String soapXmlString, JsonType jsonType){
        SOAPMessage msgs =  formatSoapString(soapXmlString);

         return soapXmlToJson(msgs,jsonType);

    }
    public static Object soapXmlToJson(SOAPMessage msgs, JsonType jsonType){

        try {
            //Document document = XmlUtils.parseXml(soapXmlString);
            SOAPEnvelope envelope = msgs.getSOAPPart().getEnvelope();
            // Element element = document.getDocumentElement();
            return JsonTypeUtils.xmlNodeToJson(Collections.singletonList(envelope),jsonType);
        } catch (Exception e) {
            logger.error("soap.err_parse_response",e);
            throw new StdException("soap.err_parse_response",e);
        }

    }
    public static WebServiceError soapErrorToWsError(SOAPFault fault){
        WebServiceError error = new WebServiceError();
        error.setFaultCode(fault.getFaultCode());
        error.setFaultActor(fault.getFaultActor());
        error.setFaultString(fault.getFaultString());
        Detail detail = fault.getDetail();
        if(detail != null){
            NodeList childNodes = detail.getChildNodes();
            if(childNodes.getLength() > 0){
                WebServiceError.FaultDetail faultDetail = new WebServiceError.FaultDetail();
                faultDetail.setName(childNodes.item(0).getLocalName());
                faultDetail.setDesc(JsonTypeUtils.xmlNodeToJson(childNodes.item(0)));
                error.setDetail(faultDetail);
            }
        }
        return error;
    }
    public static SOAPMessage formatSoapString(String soapString) {
        MessageFactory msgFactory;
        try
        {
            msgFactory = MessageFactory.newInstance();
            SOAPMessage reqMsg =
                    msgFactory.createMessage(new MimeHeaders(),
                            new ByteArrayInputStream(soapString.getBytes(Charset.forName("UTF-8"))));
            reqMsg.saveChanges();
            return reqMsg;
        }
        catch (Exception e)
        {
            throw StdException.adapt(e);
        }
    }
    public static boolean isWrappedMessage(JsonType envelopType,String opName){

        ObjectJsonType bodyType = (ObjectJsonType) JsonTypeUtils.get(envelopType,"Body");
        if(bodyType == null) return false;
        if(bodyType.getChildren() != null && bodyType.getChildren().size() ==1){
            return bodyType.getChildren().get(0).getName().equalsIgnoreCase(opName) || StringUtils.isEmpty(opName);
        }
        return false;
    }
    public static void main(String[] args) throws SOAPException {
        String faultXml = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><soap:Body><soap:Fault><faultcode>soap:Server</faultcode><faultstring>Fault occurred while processing.</faultstring><detail><ns1:UserDefineException xmlns:ns1=\"http://service.workflow.jd.com/\"><message xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns2=\"http://service.workflow.jd.com/\" xsi:nil=\"true\"/><data xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:ns2=\"http://service.workflow.jd.com/\" xsi:nil=\"true\"/><code xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns:ns2=\"http://service.workflow.jd.com/\" xsi:type=\"xs:int\">0</code></ns1:UserDefineException></detail></soap:Fault></soap:Body></soap:Envelope>";
        SOAPMessage soapMessage = formatSoapString(faultXml);
        SOAPFault fault = soapMessage.getSOAPPart().getEnvelope().getBody().getFault();
        WebServiceError webServiceError = soapErrorToWsError(fault);
        System.out.println(JsonUtils.toJSONString(webServiceError));

    }
}
