package com.jd.workflow.soap.example.custom;


/*import com.jd.workflow.soap.SoapBuilderContext;
import com.jd.workflow.soap.builder.SoapBuilder;
import com.jd.workflow.soap.builder.SoapMessageBuilder;
import com.jd.workflow.soap.builder.impl.SoapBuilderImpl;
import com.jd.workflow.soap.core.Input;*/
import com.jd.workflow.soap.client.core.SoapClient;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.wsdl.*;
import javax.xml.soap.*;
import javax.xml.namespace.QName;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

import java.io.ByteArrayInputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;

/**
 * 处理文档:https://www.w3.org/TR/2000/NOTE-SOAP-20000508/#_Toc478383507
 */
public class WebServiceProcessor implements Processor {
    static final Logger logger = LoggerFactory.getLogger(WebServiceProcessor.class);
    static String wsdlURL ="http://localhost:8000/camel-example-cxf-tomcat/webservices/incident?wsdl";
    static String endpointUrl ="http://localhost:8000/camel-example-cxf-tomcat/webservices/incident";
    Binding getBinding(Definition definition,String name){
        for (Object o : definition.getBindings().entrySet()) {
            Map.Entry<QName,Binding> entry = (Map.Entry<QName, Binding>) o;
            if(entry.getKey().getLocalPart().equals(name)){
                return entry.getValue();
            }
        }

        return null;
    }
    public BindingOperation getBindOperation(Binding binding,String operationName){
        for (Object bindingOperation : binding.getBindingOperations()) {
            BindingOperation op = (BindingOperation) bindingOperation;
            if(op.getName().equals(operationName)){
                return op;
            }
        }
        return null;
    }

    public static void main(String[] args) {
        String reqBody = "{\"arg0\":{\"details\":1,\"email\":\"wangjingfang3@jd.com\"},\"arg1\":{\"id\":\"ava\"}}";
        OperationCallInfo callInfo = new OperationCallInfo();
        callInfo.setBindingName("IncidentServiceSoapBinding");
        callInfo.setInput(reqBody);
        callInfo.setOperationName("reportIncident");
        WebServiceProcessor processor = new WebServiceProcessor();
        String result = processor.buildReqMsg(callInfo,null);
        System.out.println(result);
    }
    String buildReqMsg(OperationCallInfo operationCallInfo,Definition definition){
      /*  if(definition == null) {
            definition = parseWsdl(wsdlURL);
        }
        Binding binding = getBinding(definition, operationCallInfo.getBindingName());
        BindingOperation operation = getBindOperation(binding, operationCallInfo.getOperationName());

        SoapBuilderContext context = SoapBuilderContext.builder().input(Input.from(operationCallInfo.getInput())).build();

        SoapMessageBuilder messageBuilder = null;
        try {
            messageBuilder = new SoapMessageBuilder(new URL(wsdlURL));
        } catch (Exception e) {
             logger.error("invalid wsdlUrl:url={}",wsdlURL,e);
        }
        SoapBuilder soapBuilder = new SoapBuilderImpl(messageBuilder,binding,context);

        String result = soapBuilder.buildInputMessage(operation);
        return result;*/
        return null;
    }
    @Override
    public void process(Exchange exchange) throws Exception {

        Definition definition = parseWsdl(wsdlURL);

        OperationCallInfo operationCallInfo = exchange.getIn().getBody(OperationCallInfo.class);

        String req = buildReqMsg(operationCallInfo,definition);
        System.out.println(req);
        /*QName serviceName = null;
        QName portName = null;
        javax.wsdl.Service serviceElm = null;
        for (Object o : definition.getAllServices().entrySet()) {
            Map.Entry<QName,javax.wsdl.Service> entry = (Map.Entry) o;
            if(serviceName == null){
                serviceName = entry.getValue().getQName();
                serviceElm = entry.getValue();
            }
        }
        Service service = Service.create(new URL(wsdlURL),serviceName);
        for (Object o : serviceElm.getPorts().entrySet()) {
            Map.Entry<QName, Port> entry = (Map.Entry<QName, Port>) o;
            if(portName == null) {
                portName = new QName(definition.getTargetNamespace(),entry.getValue().getName());
            }
        }

        Dispatch<Source> disp = service.createDispatch(portName, Source.class, Service.Mode.PAYLOAD);
*/
        SoapClient client = SoapClient.builder().endpointUri(endpointUrl).build();

        String result =  client.post(req);
        logger.info("result_is:"+result);
        exchange.getOut().setBody(result);
        System.out.println("result_is"+result);
        exchange.getIn().setBody(result);
        String ret = formatResponse(result);
        System.out.println(ret);
        exchange.getIn().setBody(ret);
        exchange.getOut().setBody(ret);

    }
    //iterator 递归转json
    private static void getData(Iterator<Node> iterator, Object json) {
        while (iterator.hasNext()) {
            Object o = iterator.next();
            if (o != null) {
                SOAPElement element = null;
                try {
                    element = (SOAPElement) o;
                    //json.put(element.getNodeName(),element.getValue());
                } catch (Exception e) {
                }
                if (element != null) {
                    getData(element.getChildElements(),json);
                }
            }
        }
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
            return null;
        }
    }
    String formatResponse(String soapString){

        SOAPMessage msgs =  formatSoapString(soapString);
        SOAPBody body = null;
        try {
            SOAPEnvelope envelope = msgs.getSOAPPart().getEnvelope();
            body = envelope.getBody();
        } catch (SOAPException e) {
            e.printStackTrace();
        }
        Iterator itr= body.getChildElements();

        /*JSONObject json = new JSONObject();
        getData(itr,json);
        System.out.println(json.toJSONString());
        return json.toJSONString();*/
        return null;
    }
    public Definition parseWsdl(String wsdlUrl){
        Definition definition = null;
        WSDLReader reader = null;
        try {
            //validateWsdl(wsdlUrl);
            reader = WSDLFactory.newInstance().newWSDLReader();
            definition = reader.readWSDL(wsdlUrl);
            return definition;
        } catch (Exception e) {
            // TODO 自动生成的 catch 块
            logger.error("error occur when read wsdl!", e);
            return null;
        }
    }
}
