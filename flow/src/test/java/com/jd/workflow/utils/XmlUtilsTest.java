package com.jd.workflow.utils;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.utils.TransformUtils;

import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.jd.workflow.soap.common.xml.XNode;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.HashMap;
import java.util.Map;

public class XmlUtilsTest extends BaseTestCase {
    String xml1 = "<a>213</a>";
    String xmlLine = "<a>\n<!-- sdf -->213\n</a>";
    String namespaceXml = "<test:a xmlns:test=\"fsd\">\n213\n</test:a>";
    String namespaceAttrXml = "<test:a xmlns:test=\"fsd\" test:b=\"1\">\n213\n</test:a>";
    /**
     *  {
     *      b:{
     *          a:[213,213]
     *      }
     *  }
     */
    String xml2 = "<b><a>213</a><a>213</a></b>";
    /*
      {
        b:{
            a:[{id:1,#text:213},{id:1,#text:213}]
        }
       }
     */
    String xmlNestObjectArr = "<b><a id=\"1\">213</a><a >213</a></b>";
    String complexXmlContent1 = "<b><a id=\"1\">213<c>21</c>123</a></b>";
    String complexXmlContent2 = "<b><a id=\"1\">\n213\n<c>21\n</c>\n</a></b>";
    String nestObj = "<b><a id=\"1\"><c><d>213</d></c></a></b>";
    String hasLine = "<b><a id=\"1\">\n<c>\n<d>213</d>\n</c></a></b>";
    String listObject = "<root><persons><id>123</id><name>123</name></persons><persons><id>123</id><name>123</name></persons></root>";

    @Test
    public void testParseXml() {
        Document document = XmlUtils.parseXml(xml1);
        String result = XmlUtils.writeXml(document);
        System.out.println(result);
    }

    @Test
    public void testParseMultiNodeXml() {
        Document document = XmlUtils.parseXml(xml2);
        String result = XmlUtils.writeXml(document);
        System.out.println(result);
    }

    XNode toXNode(Element node) {
        XNode xNode = XNode.make(node.getLocalName());

        int length = node.getChildNodes().getLength();
        if (length == 1 && node.getChildNodes().item(0).getNodeValue() != null) {
            return xNode.content(node.getChildNodes().item(0).getNodeValue());
        }
        for (int i = 0; i < length; i++) {
            Node child = node.getChildNodes().item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                XNode childNode = toXNode(node);

                xNode.appendChild(childNode);

            }

        }
        return xNode;
    }

    @Test
    public void testXPath() throws Exception {

        // 解析文件，生成document对象

        String bookContent = getResourceContent("classpath:xml/bookstore.xml");
        Document document = XmlUtils.parseXml(bookContent);

        // 生成XPath对象
        XPath xpath = XPathFactory.newInstance().newXPath();

        // 获取节点值
        String webTitle = (String) xpath.evaluate(
                "/bookstore/book[@category='WEB']/title/text()", document,
                XPathConstants.STRING);
        System.out.println(webTitle);

        System.out.println("===========================================================");

        // 获取节点属性值
        String webTitleLang = (String) xpath.evaluate(
                "/bookstore/book[@category='WEB']/title/@lang", document,
                XPathConstants.STRING);
        System.out.println(webTitleLang);

        System.out.println("===========================================================");

        // 获取节点对象
        Node bookWeb = (Node) xpath.evaluate(
                "/bookstore/book[@category='WEB']", document,
                XPathConstants.NODE);
        System.out.println(bookWeb.getNodeName());

        System.out.println("===========================================================");

        // 获取节点集合
        NodeList books = (NodeList) xpath.evaluate("/bookstore/book", document,
                XPathConstants.NODESET);
        for (int i = 0; i < books.getLength(); i++) {
            Node book = books.item(i);
            System.out.println(xpath.evaluate("@category", book,
                    XPathConstants.STRING));
        }

        System.out.println("===========================================================");
    }

    @Test
    public void simpleXmlToMap(){
        Map<String, Object> map = TransformUtils.parseXml(xmlLine);
        assertEquals("{\"a\":\"213\"}", JsonUtils.toJSONString(map));
        assertEquals("<a>213</a>",TransformUtils.jsonToXml(map));
    }
    @Test
    public void listObjectToMap(){
        Map<String, Object> map = TransformUtils.parseXml(listObject);
        assertEquals("{\"root\":{\"persons\":[{\"id\":\"123\",\"name\":\"123\"},{\"id\":\"123\",\"name\":\"123\"}]}}", JsonUtils.toJSONString(map));
        assertEquals(listObject,TransformUtils.jsonToXml(map));
    }
    @Test
    public void namespaceXmlToMap(){
        Map<String, Object> map = TransformUtils.parseXml(namespaceXml);
        assertEquals("{\"a\":\"213\"}", JsonUtils.toJSONString(map));

        assertEquals("<a>213</a>",TransformUtils.jsonToXml(map));
    }
    @Test
    public void parseXmlPart(){
        String xml = "<a>123</a><a>123</a>";
        String xml1 = "<a>123</a>";
        Object o = XmlUtils.parseXmlPart(xml,true);
        assertEquals("{\"a\":[\"123\",\"123\"]}",JsonUtils.toJSONString(o));
        Object o1 = XmlUtils.parseXmlPart(xml1,true,"/a");
        assertEquals("{\"a\":[\"123\"]}",JsonUtils.toJSONString(o1));
    }
    @Test
    public void namespaceAttrXmlToMap(){
        Map<String, Object> map = TransformUtils.parseXml(namespaceAttrXml);
        assertEquals("{\"a\":{\"b\":\"1\",\"#text\":\"213\"}}", JsonUtils.toJSONString(map));

        assertEquals("<a>213<b>1</b></a>",TransformUtils.jsonToXml(map));
    }
    @Test
    public void nest(){
        Map<String, Object> map = TransformUtils.parseXml(xml2);
        assertEquals("{\"b\":{\"a\":[\"213\",\"213\"]}}", JsonUtils.toJSONString(map));

        assertEquals(xml2,TransformUtils.jsonToXml(map));
    }
    @Test
    public void xmlNestObjectArr1(){
        Map<String, Object> map = TransformUtils.parseXml(complexXmlContent1);
        assertEquals("{\"b\":{\"a\":{\"id\":\"1\",\"#text\":[\"213\",\"123\"],\"c\":\"21\"}}}", JsonUtils.toJSONString(map));

        assertEquals("<b><a>[213, 123]<id>1</id><c>21</c></a></b>",TransformUtils.jsonToXml(map));
    }

    @Test
    public void xmlNestObjectArr2(){
        Map<String, Object> map = TransformUtils.parseXml(complexXmlContent2);
        assertEquals("{\"b\":{\"a\":{\"id\":\"1\",\"#text\":\"213\",\"c\":\"21\"}}}", JsonUtils.toJSONString(map));
        assertEquals("<b><a>213<id>1</id><c>21</c></a></b>",TransformUtils.jsonToXml(map));
    }
    @Test
    public void testJsonToXmlOrder(){
        String str = "{\"b\":1,\"a\":2,\"c\":3}";
        Map map = JsonUtils.parse(str, Map.class);
        System.out.println("--------------====");
        String result = TransformUtils.jsonToXml(map);
        assertEquals("<b>1</b><a>2</a><c>3</c>",result);
        System.out.println(TransformUtils.jsonToXml(map));
    }
    @Test
    public void xmlTransformAll(){
        String str = "{\"a\":{\"#attr\":{\"attr\":1},\"b\":[\"1\",2],\"c\":[{\"d\":1},{\"d\":2}],\"d\":{\"e\":\"1\"}}}";
        assertEquals("<a attr=\"1\"><b>1</b><b>2</b><c><d>1</d></c><c><d>2</d></c><d><e>1</e></d></a>",TransformUtils.jsonToXml(JsonUtils.parse(str,Map.class)));
    }
    @Test
    public void assignArr(){
        Map<String, Object> map = TransformUtils.parseXml(nestObj,"/b/a","/b/a/c","/b/a/c/d");
        assertEquals("{\"b\":{\"a\":[{\"id\":\"1\",\"c\":[{\"d\":[\"213\"]}]}]}}", JsonUtils.toJSONString(map));
    }
    @Test
    public void hasLine(){
        Map<String, Object> map = TransformUtils.parseXml(hasLine);
        assertEquals("{\"b\":{\"a\":{\"id\":\"1\",\"c\":{\"d\":\"213\"}}}}", JsonUtils.toJSONString(map));
    }
    @Test
    public void testSelectXNode() throws XPathExpressionException {
        String text = getResourceContent("classpath:xml/bookstore.xml");
        String textSelector = "/bookstore/book[@category='WEB']/title/text()";
        String textAttr = "/bookstore/book[@category='WEB']/title/@lang";
        String node = "/bookstore/book[@category='WEB']";
        String nodeList = "/bookstore/book";
       assertEquals("Learning XML",TransformUtils.xpathSelectString(text,textSelector));
       assertEquals("en",TransformUtils.xpathSelectString(text,textAttr));
       assertEquals("{\"book\":{\"category\":\"WEB\",\"title\":{\"lang\":\"en\",\"#text\":\"Learning XML\"},\"author\":\"Erik T. Ray\",\"year\":\"2003\",\"price\":\"39.95\"}}",JsonUtils.toJSONString(TransformUtils.xpathSelectNode(text,node)));
       assertEquals("[{\"category\":\"COOKING\",\"title\":{\"lang\":\"en\",\"#text\":\"Everyday Italian\"},\"author\":\"Giada De Laurentiis\",\"year\":\"2005\",\"price\":\"30.00\"},{\"category\":\"CHILDREN\",\"title\":{\"lang\":\"en\",\"#text\":\"Harry Potter\"},\"author\":\"J K. Rowling\",\"year\":\"2005\",\"price\":\"29.99\"},{\"category\":\"WEB\",\"title\":{\"lang\":\"en\",\"#text\":\"Learning XML\"},\"author\":\"Erik T. Ray\",\"year\":\"2003\",\"price\":\"39.95\"}]",JsonUtils.toJSONString(TransformUtils.xpathSelectNodeList(text,nodeList)));

    }
    String template = "<a>${a}</a>";
    @Test
    public void renderXmlTest(){
        Map<String,Object> vars = new HashMap<>();
        vars.put("a",1);
        String xml = TransformUtils.render(template, vars);
        assertEquals("<a>1</a>",xml);

        vars.put("a",null);
         xml = TransformUtils.render(template, vars);

        assertEquals("<a></a>",xml);

        vars.put("a","<b></b>");
        xml = TransformUtils.render(template, vars);

        assertEquals("<a><b></b></a>",xml);
    }
    @Test
    public void testAppendXml1(){
        XNode a = XNode.make("a");
        String child = "<b xmlns:d=\"a\" d:d=\"c\">12</b><ns:c xmlns:ns=\"cs\" t=\"1\">vvv</ns:c>";
        XNode content = a.appendChildren(child);
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<a>\n" +
                "   <b xmlns:d=\"a\" d:d=\"c\">12</b>\n" +
                "   <ns:c xmlns:ns=\"cs\" t=\"1\">vvv</ns:c>\n" +
                "</a>",content.toXml());
    }
    @Test
    public void testAppendXml2(){
        XNode a = XNode.make("a");
        String child = "<b xmlns:d=\"a\" d:d=\"c\">12</b>";
        XNode content = a.appendChildren(child);
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<a>\n" +
                "   <b xmlns:d=\"a\" d:d=\"c\">12</b>\n" +
                "</a>",content.toXml());
    }

}
