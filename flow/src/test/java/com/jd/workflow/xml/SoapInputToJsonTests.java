package com.jd.workflow.xml;

import com.jd.workflow.WebServiceBaseTestCase;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.xml.SoapOperationToJsonTransformer;
import com.jd.workflow.flow.xml.SoapUtils;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.legacy.SoapMessageBuilder;
import org.junit.Test;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.net.URL;

public class SoapInputToJsonTests extends WebServiceBaseTestCase {
    @Test
    public void testXmlToJson() throws Exception {
        String content = getResourceContent("classpath:json/FullTypedTestData.xml");
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(new URL(wsdlUrl));
        Definition definition = readWsdlUrl();
        Binding binding = definition.getBinding(new QName("http://service.workflow.jd.com/", "FullTypedWebServiceServiceSoapBinding"));
        BindingOperation bindingOperation = binding.getBindingOperation("test", "test", "testResponse");

        BuilderJsonType outputSchema = transformer.buildSoapMessageFromOutput(binding,
                bindingOperation, SoapContext.DEFAULT);
        BuilderJsonType inputSchema = transformer.buildSoapMessageFromInput(binding,
                bindingOperation, SoapContext.DEFAULT);
        // 输出json
        System.out.println(JsonUtils.toJSONString(outputSchema.toJson()));
        System.out.println(JsonUtils.toJSONString(inputSchema.toJson()));
        Object data = SoapUtils.soapXmlToJson(content, outputSchema.toJsonType());
        System.out.println(JsonUtils.toJSONString(data));
    }

    @Test
    public void testParseDef() throws Exception {
        Definition definition = readWsdlUrl();

        SoapMessageBuilder builder = new SoapMessageBuilder(definition);

        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(definition);
        Binding binding = definition.getBinding(new QName("http://service.workflow.jd.com/", "FullTypedWebServiceServiceSoapBinding"));
        BindingOperation bindingOperation = binding.getBindingOperation("test", "test", "testResponse");

        String result = builder.buildSoapMessageFromInput(binding,
                bindingOperation, SoapContext.DEFAULT);

        System.out.println(result);

        BuilderJsonType builderJsonType = transformer.buildSoapMessageFromInput(binding,
                bindingOperation, SoapContext.DEFAULT);
        System.out.println(JsonUtils.toJSONString(builderJsonType.toJsonType()));
    }
}
