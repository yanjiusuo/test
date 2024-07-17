package com.jd.workflow.xml;

import com.jd.workflow.WebServiceBaseTestCase;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.xml.SchemaTypeToJsonType;
import com.jd.workflow.soap.xml.SoapOperationToJsonTransformer;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.BuilderJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.SoapContext;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.legacy.SampleXmlUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.xmlbeans.*;
import org.junit.Test;
import org.w3c.dom.Node;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.xml.namespace.QName;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
public class WsdlSchemaTypeToJsonTests extends WebServiceBaseTestCase {


    /**
     * 比较2个node的结构是否完全一致
     *
     * @param node1
     * @param node2
     */
    public void assertEqualsTagName(XNode node1, XNode node2) {
        if (!node1.tagNameWithoutNs().equals(node2.tagNameWithoutNs())) {
            throw new StdException("unmatch_tag_node").param("tagName1", node1.tagName()).param("tagName2", node2.tagName());
        }
        ;
        if (node1.getChildren().size() != node2.getChildren().size()) {
            throw new StdException("unmatch_children_size").param("tagName1", node1.tagName()).param("tagName2", node2.tagName());
        }
        for (XNode child1 : node1.getChildren()) {
            XNode child2 = node2.childByTag(child1.tagName());
            if (child2 == null) {
                throw new StdException("unmatched_child").param("tagName1", child1.tagName());
            }
            assertEqualsTagName(child1, child2);
        }
    }
    @Test
    public void toDemoXmlNode(){
        String objType = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"name\",\"type\":\"string\"}]}";
        JsonType jsonType = JsonUtils.parse(objType,JsonType.class);
        XNode xNode = jsonType.toDemoXmlNode();
        System.out.println(xNode.toXml());
    }

    /**
     * 测试通过soap-ui生成的文档结构与 {@link SchemaTypeToJsonType} 生成的结构是一致的
     *
     * @throws Exception
     */
    @Test
    public void testXsdToJson() throws Exception {
        SchemaType schemaType = loadSchemaType(new QName("http://service.workflow.jd.com/", "test"));

        SchemaTypeToJsonType tools = new SchemaTypeToJsonType(false);
        JsonType jsonType = tools.createJsonType(schemaType);
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
        XNode demoXNode = jsonType.toDemoXmlNode();
        log.info("demoXml={}",demoXNode.toXml());
        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();
        cursor.toNextToken();
        cursor.insertElement("test");
        cursor.toPrevToken();
        // 通过soap-ui生成xml描述
        sampleXmlUtil.createSampleForType(schemaType, cursor);


        XNode sampleNode = toXNode(object);
        //System.out.println(toString(object));
        System.out.println("sampleNode.toXml()=" + sampleNode.toXml());
        assertEqualsTagName(demoXNode, sampleNode);

        // System.out.println(sample);

    }

    @Test
    public void testSoapEncodingXmlToJson() throws Exception {
        SchemaType schemaType = loadSchemaType(new QName("http://service.workflow.jd.com/", "test"));

        SchemaTypeToJsonType tools = new SchemaTypeToJsonType(true);
        JsonType jsonType = tools.createJsonType(schemaType);
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
        XNode demoXNode = jsonType.toDemoXmlNode();
        System.out.println(demoXNode.toXml());
        XmlObject object = XmlObject.Factory.newInstance();
        XmlCursor cursor = object.newCursor();

        cursor.toNextToken();
        cursor.insertElement("test");
        cursor.toPrevToken();
        sampleXmlUtil = new SampleXmlUtil(true, SoapContext.DEFAULT);
        // 通过soap-ui生成xml描述
        sampleXmlUtil.createSampleForType(schemaType, cursor);

        System.out.println(toString(object));
        XNode sampleNode = toXNode(object);
        //System.out.println(toString(object));

        assertEqualsTagName(demoXNode, sampleNode);

        // System.out.println(sample);

    }

    XNode toXNode(XmlObject object) {
        Node domNode = object.getDomNode();

        return toXNode(domNode.getChildNodes().item(0));
    }

    XNode toXNode(Node node) {
        XNode xNode = XNode.make(node.getLocalName());

        int length = node.getChildNodes().getLength();
        if (length == 1 && node.getChildNodes().item(0).getNodeValue() != null) {
            return xNode.content(node.getChildNodes().item(0).getNodeValue());
        }
        for (int i = 0; i < length; i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                XNode childNode = toXNode(child);

                xNode.appendChild(childNode);

            }

        }
        return xNode;
    }

    String toString(XmlObject object) {
        XmlOptions options = new XmlOptions();
        options.put(XmlOptions.SAVE_PRETTY_PRINT);
        options.put(XmlOptions.SAVE_PRETTY_PRINT_INDENT, 3);
        options.put(XmlOptions.SAVE_AGGRESSIVE_NAMESPACES);
        options.setSaveOuter();
        String result = object.xmlText(options);

        return result;
    }


    /**
     * 测试soap输入转换为Json类型是否正确
     */
    @Test
    public void testSoapInputToJsonType() throws Exception {
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(new URL(wsdlUrl));
        Definition definition = transformer.getDefinition();

        Binding binding = definition.getBinding(new QName("http://service.workflow.jd.com/", "FullTypedWebServiceServiceSoapBinding"));
        BindingOperation bindingOperation = binding.getBindingOperation("test", "test", "testResponse");
        BuilderJsonType schemaType = transformer.buildSoapMessageFromInput(binding,
                bindingOperation, SoapContext.DEFAULT);
        JsonType jsonType = schemaType.toJsonType();
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
        System.out.println(jsonType.toDemoXmlNode().toXml());

        System.out.println(JsonUtils.toJSONString(jsonType.toDescJson()));

        String element = soapMessageBuilder.buildSoapMessageFromInput(binding, bindingOperation, SoapContext.DEFAULT);
        System.out.println("element=" + element);
    }

    /**
     * 测试soap输出转换为Json类型是否正确
     */
    @Test
    public void testSoapOutputToJsonType() throws Exception {
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(new URL(wsdlUrl));
        Definition definition = transformer.getDefinition();

        Binding binding = definition.getBinding(new QName("http://service.workflow.jd.com/", "FullTypedWebServiceServiceSoapBinding"));
        BuilderJsonType schemaType = transformer.buildSoapMessageFromOutput(binding,
                binding.getBindingOperation("test", "test", "testResponse"), SoapContext.DEFAULT);
        JsonType jsonType = schemaType.toJsonType();
        System.out.println(JsonUtils.toJSONString(jsonType.toJson()));
        log.info("demoXmlNode_={}",jsonType.toDemoXmlNode().toXml());

    }

    /**
     * 测试将json数据转换为xml
     */
    @Test
    public void testJsonTypeToXml() throws Exception {
        SoapOperationToJsonTransformer transformer = new SoapOperationToJsonTransformer(new URL(wsdlUrl));
        Definition definition = transformer.getDefinition();

        Binding binding = definition.getBinding(new QName("http://service.workflow.jd.com/", "FullTypedWebServiceServiceSoapBinding"));
        BuilderJsonType schemaType = transformer.buildSoapMessageFromInput(binding,
                binding.getBindingOperation("test", "test", "testResponse"), SoapContext.DEFAULT);
        JsonType jsonType = schemaType.toJsonType();

        log.info("xml_json_type={}",JsonUtils.toJSONString(jsonType.toJson()));
        System.out.println(jsonType.toDemoXmlNode().toXml());


        String content = getResourceContent("classpath:json/FullTypedTestData.json");

        XNode root = XNode.make("root");
        Map<String, Object> envelop = new HashMap<>();
        Map<String, Object> body = new HashMap<>();
        body.put("test", JsonUtils.parse(content, Map.class));
        envelop.put("Body", body);
        List<XNode> children = schemaType.transformToXml(envelop);
        root.appendChildren(children);
        log.info("outputXml={}",root.toXml());
        String result = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<root>\n" +
                "   <soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"\n" +
                "                     xmlns:ser=\"http://service.workflow.jd.com/\">\n" +
                "      <soapenv:Body>\n" +
                "         <ser:test>\n" +
                "            <arg0>string</arg0>\n" +
                "            <arg1>\n" +
                "               <bigDecimalVar>12.12</bigDecimalVar>\n" +
                "               <bigIntegerVar>13</bigIntegerVar>\n" +
                "               <booleanVar>true</booleanVar>\n" +
                "               <byteVar>13</byteVar>\n" +
                "               <charVar>13</charVar>\n" +
                "               <child>\n" +
                "                  <booleanVar>true</booleanVar>\n" +
                "                  <charVar>13</charVar>\n" +
                "                  <doubleVar>123.113</doubleVar>\n" +
                "                  <floatVar>123.112</floatVar>\n" +
                "                  <intVar>13</intVar>\n" +
                "                  <longVar>1321</longVar>\n" +
                "                  <shortVar>13</shortVar>\n" +
                "                  <strVar>string</strVar>\n" +
                "                  <subChild>\n" +
                "                     <intVar>13</intVar>\n" +
                "                  </subChild>\n" +
                "               </child>\n" +
                "               <childHashMap>\n" +
                "                  <entry>\n" +
                "                     <key>string</key>\n" +
                "                     <value>\n" +
                "                        <booleanVar>true</booleanVar>\n" +
                "                        <charVar>13</charVar>\n" +
                "                        <doubleVar>123.113</doubleVar>\n" +
                "                        <floatVar>123.112</floatVar>\n" +
                "                        <intVar>13</intVar>\n" +
                "                        <longVar>1321</longVar>\n" +
                "                        <shortVar>13</shortVar>\n" +
                "                        <strVar>string</strVar>\n" +
                "                        <subChild>\n" +
                "                           <intVar>13</intVar>\n" +
                "                        </subChild>\n" +
                "                     </value>\n" +
                "                  </entry>\n" +
                "               </childHashMap>\n" +
                "               <childList>\n" +
                "                  <booleanVar>true</booleanVar>\n" +
                "                  <charVar>13</charVar>\n" +
                "                  <doubleVar>123.113</doubleVar>\n" +
                "                  <floatVar>123.112</floatVar>\n" +
                "                  <intVar>13</intVar>\n" +
                "                  <longVar>1321</longVar>\n" +
                "                  <shortVar>13</shortVar>\n" +
                "                  <strVar>string</strVar>\n" +
                "                  <subChild>\n" +
                "                     <intVar>13</intVar>\n" +
                "                  </subChild>\n" +
                "               </childList>\n" +
                "               <children>\n" +
                "                  <booleanVar>true</booleanVar>\n" +
                "                  <charVar>13</charVar>\n" +
                "                  <doubleVar>123.113</doubleVar>\n" +
                "                  <floatVar>123.112</floatVar>\n" +
                "                  <intVar>13</intVar>\n" +
                "                  <longVar>1321</longVar>\n" +
                "                  <shortVar>13</shortVar>\n" +
                "                  <strVar>string</strVar>\n" +
                "                  <subChild>\n" +
                "                     <intVar>13</intVar>\n" +
                "                  </subChild>\n" +
                "               </children>\n" +
                "               <childrenChildren>\n" +
                "                  <item>\n" +
                "                     <booleanVar>true</booleanVar>\n" +
                "                     <charVar>13</charVar>\n" +
                "                     <doubleVar>123.113</doubleVar>\n" +
                "                     <floatVar>123.112</floatVar>\n" +
                "                     <intVar>13</intVar>\n" +
                "                     <longVar>1321</longVar>\n" +
                "                     <shortVar>13</shortVar>\n" +
                "                     <strVar>string</strVar>\n" +
                "                     <subChild>\n" +
                "                        <intVar>13</intVar>\n" +
                "                     </subChild>\n" +
                "                  </item>\n" +
                "               </childrenChildren>\n" +
                "               <doubleVar>123.113</doubleVar>\n" +
                "               <floatVar>123.112</floatVar>\n" +
                "               <intVar>13</intVar>\n" +
                "               <longVar>1321</longVar>\n" +
                "               <shortVar>13</shortVar>\n" +
                "               <strArrArr>\n" +
                "                  <item>string</item>\n" +
                "               </strArrArr>\n" +
                "               <strArrArrArr>\n" +
                "                  <item>\n" +
                "                     <item>string</item>\n" +
                "                  </item>\n" +
                "               </strArrArrArr>\n" +
                "               <strArrArrArrArr>\n" +
                "                  <item>\n" +
                "                     <item>\n" +
                "                        <item>string</item>\n" +
                "                     </item>\n" +
                "                  </item>\n" +
                "               </strArrArrArrArr>\n" +
                "               <strArray>string</strArray>\n" +
                "               <strVar>string</strVar>\n" +
                "               <stringHashMap>\n" +
                "                  <entry>\n" +
                "                     <key>string</key>\n" +
                "                     <value>string</value>\n" +
                "                  </entry>\n" +
                "               </stringHashMap>\n" +
                "               <stringList>string</stringList>\n" +
                "            </arg1>\n" +
                "         </ser:test>\n" +
                "      </soapenv:Body>\n" +
                "   </soapenv:Envelope>\n" +
                "</root>";
        assertEquals(result,root.toXml());

    }

    private ObjectJsonType getInputType(ObjectJsonType soapRoot) {
        ObjectJsonType body = null;
        for (JsonType child : soapRoot.getChildren()) {
            if (child.getName().equals("Body")) {
                body = (ObjectJsonType) child;
            }
        }
        return (ObjectJsonType) body.getChildren().get(0);
    }
}
