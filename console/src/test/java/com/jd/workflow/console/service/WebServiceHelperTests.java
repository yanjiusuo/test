package com.jd.workflow.console.service;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.EasyWebService;
import com.jd.workflow.console.dto.CallHttpToWebServiceReqDTO;
import com.jd.workflow.console.dto.WebServiceMethod;
import com.jd.workflow.console.helper.WebServiceHelper;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.utils.WsdlUtils;
import com.jd.workflow.soap.xml.SoapOperationToJsonTransformer;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

public class WebServiceHelperTests extends BaseTestCase {
    String publishUrl = "http://127.0.0.1:3001/FullTypedWebService";
    String wsdlUrl = publishUrl+"?wsdl";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        EasyWebService.run(wsdlUrl);
    }
    public void testDebugWebService() throws Exception {
        Definition definition = WsdlUtils.parseWsdl(wsdlUrl);
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(new URL(wsdlUrl));
         String opName = "simple";
        Binding binding = definition.getBinding(new QName("http://workflow.jd.com/", "EasyWebServiceServiceSoapBinding"));

        BindingOperation bindingOperation = binding.getBindingOperation(opName, null, null);

        BuilderJsonType input = transformer.buildSoapMessageFromInput(binding, bindingOperation, SoapContext.DEFAULT);
        BuilderJsonType inputCopy = transformer.buildSoapMessageFromInput(binding, bindingOperation, SoapContext.DEFAULT);
        BuilderJsonType output = transformer.buildSoapMessageFromOutput(binding, bindingOperation, SoapContext.DEFAULT);
        CallHttpToWebServiceReqDTO dto = new CallHttpToWebServiceReqDTO();
        dto.setInputType("json");
        JsonType jsonType = inputCopy.toJsonType();
        JsonType arg0 = JsonTypeUtils.get(jsonType, "Body", "simple", "arg0");
        JsonType arg1_id = JsonTypeUtils.get(jsonType, "Body", "simple", "arg1","id");
        JsonType arg1_name = JsonTypeUtils.get(jsonType, "Body", "simple", "arg1","name");
        arg0.setValue("123");
        arg1_id.setValue("123");
        arg1_name.setValue("123");
        dto.setInput(jsonType.toJson());

        WebServiceMethod serviceMethod = new WebServiceMethod();
        serviceMethod.setInput(WebServiceMethod.WebServiceMethodIO.builder().schemaType(input.toJsonType()).build());
        serviceMethod.setOutput(WebServiceMethod.WebServiceMethodIO.builder().schemaType(output.toJsonType()).build());
        HttpOutput httpOutput = WebServiceHelper.debugWebServiceMethod(dto,"", publishUrl, serviceMethod);
        assertTrue(httpOutput.getBody() != null);
        System.out.println(JsonUtils.toJSONString(httpOutput));
    }
}
