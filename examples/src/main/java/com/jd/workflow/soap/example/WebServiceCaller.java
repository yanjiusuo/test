package com.jd.workflow.soap.example;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.ClientImpl;
import org.apache.cxf.endpoint.Endpoint;
//import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;
import org.apache.cxf.service.model.*;

import javax.xml.namespace.QName;
import java.beans.PropertyDescriptor;
import java.util.List;

public class WebServiceCaller {
    public static void main(String[] args) throws Exception {
        /*String  wsdlURL = "http://localhost:8081/spring_cxf?wsdl";
        System.out.println(wsdlURL);

        JaxWsDynamicClientFactory factory = JaxWsDynamicClientFactory.newInstance();
        Client client = factory.createClient(wsdlURL);
        ClientImpl clientImpl = (ClientImpl) client;
        Endpoint endpoint = clientImpl.getEndpoint();
        ServiceInfo serviceInfo = endpoint.getService().getServiceInfos().get(0);
        QName bindingName = new QName("http://service.my/",
                "PersonServiceSoapBinding");
        BindingInfo binding = serviceInfo.getBinding(bindingName);
        //{
        QName opName = new QName("http://service.my/", "getPersonName");
        BindingOperationInfo boi = binding.getOperation(opName);
        BindingMessageInfo inputMessageInfo = boi.getInput();
        List<MessagePartInfo> parts = inputMessageInfo.getMessageParts();
        // only one part.
        MessagePartInfo partInfo = parts.get(0);
        Class<?> partClass = partInfo.getTypeClass();//class my.service.GetPersonName
        System.out.println(partClass.getCanonicalName()); // GetAgentDetails
        Object inputObject = partClass.newInstance();
        // Unfortunately, the slot inside of the part object is also called 'part'.
        // this is the descriptor for get/set part inside the GetAgentDetails class.
        PropertyDescriptor partPropertyDescriptor = new PropertyDescriptor("arg0", partClass);//arg0对应person类型
        // This is the type of the class which really contains all the parameter information.
        Class<?> partPropType = partPropertyDescriptor.getPropertyType(); // class my.service.Person
        System.out.println(partPropType.getCanonicalName());
        Object inputPartObject = partPropType.newInstance();
        partPropertyDescriptor.getWriteMethod().invoke(inputObject, inputPartObject);
        PropertyDescriptor numberPropertyDescriptor = new PropertyDescriptor("name", partPropType);
        numberPropertyDescriptor.getWriteMethod().invoke(inputPartObject, "张三");

        Object[] result = client.invoke(opName, inputPartObject);
        Class<?> resultClass = result[0].getClass();*/
    }
}
