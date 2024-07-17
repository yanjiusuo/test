package com.jd.workflow.soap.common.xml.schema;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.XmlString;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.w3c.dom.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(JUnit4.class)
public class JsonTypeTests extends Assert {
    @Test
    public void listObject(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"persons\",\"type\":\"array\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"name\",\"type\":\"string\"}]}]}]}";
        String strValue = "{\"persons\":[{\"id\":\"213\",\"name\":\"wjf\"},{\"id\":\"213\",\"name\":\"wjf\"}]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        System.out.println(JsonUtils.toJSONString(jsonType));
        Object data = JsonUtils.parse(strValue);
        jsonType.setValue(data);
        String xml = jsonType.transformToXml().get(0).toXml();
        System.out.println(xml);
        Map map = XmlUtils.xmlToMap(xml);
        assertEquals(strValue, JsonUtils.toJSONString(map.get("root")));
        String value = JsonUtils.toJSONString(xmlToJson(xml,jsonType));
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }
    Object xmlToJson(String xml,JsonType jsonType){
        Document document = XmlUtils.parseXml(xml);
       return  JsonTypeUtils.xmlNodeToJson(Collections.singletonList(document.getDocumentElement()),jsonType);
    }
    @Test
    public void listString(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"array\",\"children\":[{\"name\":\"id\",\"type\":\"string\"}]}]}";
        String strValue = "{\"id\":[\"1\",\"2\"]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        Object data = JsonUtils.parse(strValue);
        jsonType.setValue(data);
        String xml = jsonType.transformToXml().get(0).toXml();
        System.out.println(xml);
        Map map = XmlUtils.xmlToMap(xml);
        assertEquals(strValue, JsonUtils.toJSONString(map.get("root")));

        String value = JsonUtils.toJSONString(xmlToJson(xml,jsonType));
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }
    @Test
    public void listListObject(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"items\",\"type\":\"array\",\"children\":[{\"name\":\"item\",\"type\":\"array\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"string\"}]}]}]}]}";
        String strValue = "{\"items\":[[{\"id\":\"1\"}],[{\"id\":\"1\"}]]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        Object data = JsonUtils.parse(strValue);
        jsonType.setValue(data);
        String xml = jsonType.transformToXml().get(0).toXml();
        System.out.println(xml);
        String result = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<root>\n" +
                "   <items>\n" +
                "      <item>\n" +
                "         <id>1</id>\n" +
                "      </item>\n" +
                "   </items>\n" +
                "   <items>\n" +
                "      <item>\n" +
                "         <id>1</id>\n" +
                "      </item>\n" +
                "   </items>\n" +
                "</root>";

        assertEquals(xml, result);

        String value = JsonUtils.toJSONString(xmlToJson(xml,jsonType));
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }
    @Test
    public void extAttrsTests(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"rowType\":\"object\",\"children\":[{\"name\":\"items\",\"type\":\"array\",\"rowType\":\"123\",\"children\":[{\"name\":\"items\",\"type\":\"string\",\"rowType\":123}]}]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        String result = JsonUtils.toJSONString(jsonType);
        assertEquals("{\"name\":\"root\",\"rowType\":\"object\",\"type\":\"object\",\"children\":[{\"name\":\"items\",\"rowType\":\"123\",\"type\":\"array\",\"children\":[{\"name\":\"items\",\"rowType\":123,\"type\":\"string\"}]}]}",result);

    }
    @Test
    public void listListString(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"items\",\"type\":\"array\",\"children\":[{\"name\":\"items\",\"type\":\"array\",\"children\":[{\"name\":\"id\",\"type\":\"string\"}]}]}]}";
        String strValue = "{\"items\":[[\"123\"],[\"234\",\"345\"]]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        Object data = JsonUtils.parse(strValue);
        jsonType.setValue(data);
        String xml = jsonType.transformToXml().get(0).toXml();
        System.out.println(xml);
        String result = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<root>\n" +
                "   <items>\n" +
                "      <items>123</items>\n" +
                "   </items>\n" +
                "   <items>\n" +
                "      <items>234</items>\n" +
                "      <items>345</items>\n" +
                "   </items>\n" +
                "</root>";

        assertEquals(xml, result);

        String value = JsonUtils.toJSONString(xmlToJson(xml,jsonType));
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }
    @Test
    public void testComment(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"items\",\"type\":\"array\",\"children\":[{\"name\":\"items\",\"type\":\"array\",\"children\":[{\"name\":\"id\",\"type\":\"string\"}]}]}]}";
        String strValue = "{\"items\":[[\"123\"],[\"234\",\"345\"]]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<!--dffsdds -->"+
                "<root>\n" +
                "   <!--dffsdds -->\n" +
                "   <items>\n" +
                "      <items>123</items>\n" +
                "   </items>\n" +
                "   <items>\n" +
                "      <items>234</items>\n" +
                "      <items>345</items>\n" +
                "   </items>\n" +
                "</root>";
        Object o = xmlToJson(xml, jsonType);
        String value = JsonUtils.toJSONString(xmlToJson(xml,jsonType));
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }
    @Test
    public void listListListString(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"items\",\"type\":\"array\",\"children\":[{\"name\":\"items\",\"type\":\"array\",\"children\":[{\"name\":\"items\",\"type\":\"array\",\"children\":[{\"name\":\"id\",\"type\":\"string\"}]}]}]}]}";
        String strValue = "{\"items\":[[[\"1\"],[\"2\"],[\"1\"],[\"2\"]],[[\"1\"],[\"2\"],[\"1\"],[\"2\"]]]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        Object data = JsonUtils.parse(strValue);
        jsonType.setValue(data);
        String xml = jsonType.transformToXml().get(0).toXml();
        System.out.println(xml);
        String result = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<root>\n" +
                "   <items>\n" +
                "      <items>\n" +
                "         <items>1</items>\n" +
                "      </items>\n" +
                "      <items>\n" +
                "         <items>2</items>\n" +
                "      </items>\n" +
                "      <items>\n" +
                "         <items>1</items>\n" +
                "      </items>\n" +
                "      <items>\n" +
                "         <items>2</items>\n" +
                "      </items>\n" +
                "   </items>\n" +
                "   <items>\n" +
                "      <items>\n" +
                "         <items>1</items>\n" +
                "      </items>\n" +
                "      <items>\n" +
                "         <items>2</items>\n" +
                "      </items>\n" +
                "      <items>\n" +
                "         <items>1</items>\n" +
                "      </items>\n" +
                "      <items>\n" +
                "         <items>2</items>\n" +
                "      </items>\n" +
                "   </items>\n" +
                "</root>";

        assertEquals(xml, result);

        String value = JsonUtils.toJSONString(xmlToJson(xml,jsonType));
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }
    @Test
    public void object(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"string\"}]}";
        String strValue = "{\"id\":\"1\"}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        Object data = JsonUtils.parse(strValue);
        jsonType.setValue(data);
        String xml = jsonType.transformToXml().get(0).toXml();
        System.out.println(xml);
        Map map = XmlUtils.xmlToMap(xml);
        assertEquals(strValue, JsonUtils.toJSONString(map.get("root")));

        String value = JsonUtils.toJSONString(xmlToJson(xml,jsonType));
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }
    @Test
    public void objectObject(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"string\"}]}]}";
        String strValue = "{\"person\":{\"id\":\"123\"}}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        Object data = JsonUtils.parse(strValue);
        jsonType.setValue(data);
        String xml = jsonType.transformToXml().get(0).toXml();
        System.out.println(xml);
        Map map = XmlUtils.xmlToMap(xml);
        assertEquals(strValue, JsonUtils.toJSONString(map.get("root")));

        String value = JsonUtils.toJSONString(xmlToJson(xml,jsonType));
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }
    @Test
    public void objectObjectObject(){
        String str = "{\"name\":\"root\",\"type\":\"object\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"string\"}]}]}]}";
        String strValue = "{\"person\":{\"person\":{\"id\":\"123\"}}}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        Object data = JsonUtils.parse(strValue);
        jsonType.setValue(data);
        String xml = jsonType.transformToXml().get(0).toXml();
        System.out.println(xml);
        Map map = XmlUtils.xmlToMap(xml);
        assertEquals(strValue, JsonUtils.toJSONString(map.get("root")));

        String value = JsonUtils.toJSONString(xmlToJson(xml,jsonType));
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }
    @Test
    public void simple(){
        String str = "{\"name\":\"root\",\"type\":\"string\"}";
        String strValue = "1";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        Object data = JsonUtils.parse(strValue);
        jsonType.setValue(data);
        String xml = jsonType.transformToXml().get(0).toXml();
        System.out.println(xml);
        Map map = XmlUtils.xmlToMap(xml);
        assertEquals(strValue, map.get("root"));

        String value = (String) xmlToJson(xml,jsonType);
        log.info("xmlToJsonValue={}",value);
        assertEquals(strValue,value);
    }

    @Test(expected = JsonTypeParseException.class)
    public void parseArrayJson(){
        String text = "[{\"name\":\"array\",\"type\":\"array\",\"children\":[{\"name\":\"id\"}]}]";
        System.out.println(JsonUtils.parseArray(text,JsonType.class));;
    }
    @Test
    public void parseExprType(){
        String text = "[{\"type\":\"object\",\"name\":\"root\",\"children\":[{\"name\":\"id\",\"type\":\"integer\",\"value\":1},{\"name\":\"name\",\"type\":\"string\",\"exprType\":\"script\",\"value\":\"workflow.input.body.name\"},{\"name\":\"value\",\"type\":\"string\",\"exprType\":\"script\",\"value\":\"steps.http1.output.body.id+\\\"-\\\"+steps.http1.output.body.name\"}]}]";
        List<JsonType> jsonTypes = JsonUtils.parseArray(text, JsonType.class);
        System.out.println(jsonTypes);;
    }
    @Test
    public void transformToXmlTests(){
        SimpleJsonType simpleJsonType = new SimpleJsonType(SimpleParamType.STRING);
        simpleJsonType.setName("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<a>\n" +
                "   <b xmlns:d=\"a\" d:d=\"c\">12</b>\n" +
                "   <ns:c xmlns:ns=\"cs\" t=\"1\">vvv</ns:c>\n" +
                "</a>");
        String child = "<b xmlns:d=\"a\" d:d=\"c\">12</b><ns:c xmlns:ns=\"cs\" t=\"1\">vvv</ns:c>";
        simpleJsonType.setValue(XmlString.from(child));
        assertEquals("",simpleJsonType.transformToXml().get(0).toXml());
    }
    ExprTreeNode newExprTreeNode(){
        ExprTreeNode node = new ExprTreeNode("root","object","root");
        return node;
    }
    @Test
    public void testArraySimpleToExprNode(){
        String str = "{\"name\":\"persons\",\"type\":\"array\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"}]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        ExprTreeNode parent = newExprTreeNode();
        jsonType.buildExprNode(parent);
        assertEquals("{\"level\":0,\"key\":\"0\",\"type\":\"object\",\"label\":\"root\",\"expr\":\"root\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"array\",\"label\":\"persons\",\"expr\":\"root.persons\",\"children\":[{\"level\":2,\"key\":\"0_0_0\",\"type\":\"integer\",\"label\":\"[0]\",\"expr\":\"root.persons[0]\",\"children\":null}]}]}",JsonUtils.toJSONString(parent));
    }
    @Test
    public void testArrayArrayArrayObjectToExprNode(){
        String str = "{\"name\":\"persons\",\"type\":\"array\",\"children\":[{\"name\":\"child\",\"type\":\"array\",\"children\":[{\"name\":\"childChild\",\"type\":\"array\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"}]}]}]}]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        ExprTreeNode parent = newExprTreeNode();
        jsonType.buildExprNode(parent);
        assertEquals("",JsonUtils.toJSONString(parent));
    }
    @Test
    public void testArrayObjectToExprNode(){
        String str = "{\"name\":\"persons\",\"type\":\"array\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"}]}]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        ExprTreeNode parent = newExprTreeNode();
        jsonType.buildExprNode(parent);
        assertEquals("{\"level\":0,\"key\":\"0\",\"type\":\"object\",\"label\":\"root\",\"expr\":\"root\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"array\",\"label\":\"persons\",\"expr\":\"root.persons\",\"children\":[{\"level\":2,\"key\":\"0_0_0\",\"type\":\"object\",\"label\":\"[0]\",\"expr\":\"root.persons[0]\",\"children\":[{\"level\":3,\"key\":\"0_0_0_0\",\"type\":\"integer\",\"label\":\"id\",\"expr\":\"root.persons[0].id\",\"children\":null}]}]}]}",JsonUtils.toJSONString(parent));
    }

    @Test
    public void testStringJsonObjectToExprNode(){
        String str = "{\"name\":\"persons\",\"type\":\"string_json\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"}]}]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        ExprTreeNode parent = newExprTreeNode();
        jsonType.buildExprNode(parent);
        assertEquals("{\"level\":0,\"key\":\"0\",\"type\":\"object\",\"label\":\"root\",\"expr\":\"root\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"object\",\"label\":\"persons\",\"expr\":\"root.persons\",\"children\":[{\"level\":2,\"key\":\"0_0_0\",\"type\":\"integer\",\"label\":\"id\",\"expr\":\"root.persons.id\",\"children\":null}]}]}",JsonUtils.toJSONString(parent));
    }
    @Test
    public void testStringJsonArrayObjectToExprNode(){
        String str = "{\"name\":\"persons\",\"type\":\"string_json\",\"children\":[{\"name\":\"persons\",\"type\":\"array\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"}]}]}]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        ExprTreeNode parent = newExprTreeNode();
        jsonType.buildExprNode(parent);
        assertEquals("{\"level\":0,\"key\":\"0\",\"type\":\"object\",\"label\":\"root\",\"expr\":\"root\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"array\",\"label\":\"persons\",\"expr\":\"root.persons\",\"children\":[{\"level\":2,\"key\":\"0_0_0\",\"type\":\"object\",\"label\":\"[0]\",\"expr\":\"root.persons[0]\",\"children\":[{\"level\":3,\"key\":\"0_0_0_0\",\"type\":\"integer\",\"label\":\"id\",\"expr\":\"root.persons[0].id\",\"children\":null}]}]}]}",JsonUtils.toJSONString(parent));
    }

    @Test
    public void testStringXmlObjectToExprNode(){
        String str = "{\"name\":\"persons\",\"type\":\"string_xml\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"}]}]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        ExprTreeNode parent = newExprTreeNode();
        jsonType.buildExprNode(parent);
        assertEquals("{\"level\":0,\"key\":\"0\",\"type\":\"object\",\"label\":\"root\",\"expr\":\"root\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"string_xml\",\"label\":\"persons\",\"expr\":\"root.persons\",\"children\":[{\"level\":2,\"key\":\"0_0_0\",\"type\":\"object\",\"label\":\"person\",\"expr\":\"root.persons.person\",\"children\":[{\"level\":3,\"key\":\"0_0_0_0\",\"type\":\"integer\",\"label\":\"id\",\"expr\":\"root.persons.person.id\",\"children\":null}]}]}]}",JsonUtils.toJSONString(parent));
    }
    @Test
    public void testStringXmlArrayObjectToExprNode(){
        String str = "{\"name\":\"persons\",\"type\":\"string_xml\",\"children\":[{\"name\":\"persons\",\"type\":\"array\",\"children\":[{\"name\":\"person\",\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"}]}]}]}";
        JsonType jsonType = JsonUtils.parse(str, JsonType.class);
        ExprTreeNode parent = newExprTreeNode();
        jsonType.buildExprNode(parent);
        assertEquals("{\"level\":0,\"key\":\"0\",\"type\":\"object\",\"label\":\"root\",\"expr\":\"root\",\"children\":[{\"level\":1,\"key\":\"0_0\",\"type\":\"string_xml\",\"label\":\"persons\",\"expr\":\"root.persons\",\"children\":[{\"level\":2,\"key\":\"0_0_0\",\"type\":\"array\",\"label\":\"persons\",\"expr\":\"root.persons.persons\",\"children\":[{\"level\":3,\"key\":\"0_0_0_0\",\"type\":\"object\",\"label\":\"[0]\",\"expr\":\"root.persons.persons[0]\",\"children\":[{\"level\":4,\"key\":\"0_0_0_0_0\",\"type\":\"integer\",\"label\":\"id\",\"expr\":\"root.persons.persons[0].id\",\"children\":null}]}]}]}]}",JsonUtils.toJSONString(parent));
    }
    @Test
    public void testObjectCastValue(){
        String jsonTypeStr = "{\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"},{\"name\":\"name\",\"type\":\"string\"}]}";
        JsonType jsonType = JsonUtils.parse(jsonTypeStr, JsonType.class);
        String values = "{\"id\":1,\"name\":\"aa\",\"value\":23}";
        Object o = jsonType.castValue(values);
        assertEquals("{\"id\":1,\"name\":\"aa\",\"value\":23}",JsonUtils.toJSONString(o));
    }
    @Test
    public void testSingleArrayCastValue(){
        String jsonTypeStr = "{\"type\":\"array\",\"children\":[{\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"},{\"name\":\"name\",\"type\":\"string\"}]}]}";
        JsonType jsonType = JsonUtils.parse(jsonTypeStr, JsonType.class);
        String values = "[{\"id\":1,\"name\":\"aa\",\"value\":23},{\"id\":1,\"name\":\"aa\",\"value\":23}]";
        Object o = jsonType.castValue(values);
        assertEquals("[{\"id\":1,\"name\":\"aa\",\"value\":23},{\"id\":1,\"name\":\"aa\",\"value\":23}]",JsonUtils.toJSONString(o));
    }
    @Test
    public void testMultiArrayCastValue(){
        String jsonTypeStr = "{\"type\":\"array\",\"children\":[{\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"},{\"name\":\"name\",\"type\":\"string\"}]},{\"type\":\"object\",\"children\":[{\"name\":\"id\",\"type\":\"integer\"},{\"name\":\"name\",\"type\":\"string\"}]}]}";
        JsonType jsonType = JsonUtils.parse(jsonTypeStr, JsonType.class);
        String values = "[{\"id\":1,\"name\":\"aa\",\"value\":23},{\"id\":2,\"name\":\"aa\",\"value\":23},{\"id\":3,\"name\":\"aa\",\"value\":23}]";
        Object o = jsonType.castValue(values);
        assertEquals("[{\"id\":1,\"name\":\"aa\",\"value\":23},{\"id\":2,\"name\":\"aa\",\"value\":23},{\"id\":3,\"name\":\"aa\",\"value\":23}]",JsonUtils.toJSONString(o));
    }
}
