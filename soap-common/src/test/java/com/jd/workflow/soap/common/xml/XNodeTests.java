package com.jd.workflow.soap.common.xml;

import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class XNodeTests extends TestCase {
    public void testNoNsToXml(){
        XNode root = XNode.make("root");
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<root/>",root.toXml());
    }
    public void testNsToXml(){
        XNode root = XNode.make("root").attr("xmlns","http://com.jd");
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<root xmlns=\"http://com.jd\"/>",root.toXml());
    }

    public void testNsFullToXml(){
        XNode root = XNode.make("j:root").attr("xmlns:j","http://com.jd");
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<j:root xmlns:j=\"http://com.jd\"/>",root.toXml());
    }
    public void testDefaultNs(){
        XNode root = XNode.make("root").attr("xmlns","http://com.jd");
        root.makeChild("child").makeChild("childChild");
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<root xmlns=\"http://com.jd\">\n" +
                "   <child>\n" +
                "      <childChild/>\n" +
                "   </child>\n" +
                "</root>",root.toXml());
    }
    public void testChildNoNs(){
        XNode root = XNode.make("j:root").attr("xmlns:j","http://com.jd");
        XNode child = root.makeChild("child").attr("dfs","321");
        child.makeChild("childItem");
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<j:root xmlns:j=\"http://com.jd\">\n" +
                "   <child dfs=\"321\">\n" +
                "      <childItem/>\n" +
                "   </child>\n" +
                "</j:root>",root.toXml());
    }
    public void testNsChildToXml(){
        XNode root = XNode.make("j:root").attr("xmlns:j","http://com.jd");
        XNode child = root.makeChild("d:table").attr("xmlns:d", "http://cs.dd");
        child.makeChild("d:tr");
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<j:root xmlns:j=\"http://com.jd\">\n" +
                "   <d:table xmlns:d=\"http://cs.dd\">\n" +
                "      <d:tr/>\n" +
                "   </d:table>\n" +
                "</j:root>",root.toXml());
    }
    public void testAll(){
        XNode root = XNode.make("j:root").attr("xmlns:j","http://com.jd").attr("xmlns","http://abc");
        XNode child = root.makeChild("d:table").attr("xmlns:d", "http://cs.dd");
        child.makeChild("d:tr");
        child.makeChild("td");
        assertEquals("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<j:root xmlns:j=\"http://com.jd\" xmlns=\"http://abc\">\n" +
                "   <d:table xmlns:d=\"http://cs.dd\">\n" +
                "      <d:tr/>\n" +
                "      <td/>\n" +
                "   </d:table>\n" +
                "</j:root>",root.toXml());
    }

    public void parseXml(String xml){
        XNode node = XNode.parse(xml);
        String formatXml  = node.toXml(false,false);
        log.info("xml_result={}",formatXml);
        assertEquals(xml,formatXml);
    }
    public JsonType parseXml(String xml, String jsonTypeXml){
        XNode node = XNode.parse(xml);
        String formatXml  = node.toXml(false,false);
        log.info("xml_result={}",formatXml);
        assertEquals(xml,formatXml);
        JsonType jsonType = node.toJsonType();
        assertEquals(jsonTypeXml, JsonUtils.toJSONString(jsonType));
        return jsonType;
    }

    public void testParseSimpleXml(){

        parseXml("<a>1</a>","{\"name\":\"a\",\"className\":null,\"type\":\"integer\"}");
        parseXml("<d:a xmlns:d=\"d\" id=\"1\">1</d:a>","{\"name\":\"a\",\"namespacePrefix\":\"d:\",\"className\":null,\"attrs\":{\"id\":\"1\",\"xmlns:d\":\"d\"},\"type\":\"integer\"}");
        parseXml("<a><b>2</b><b>2</b></a>","{\"name\":\"a\",\"className\":null,\"type\":\"object\",\"children\":[{\"name\":\"b\",\"className\":null,\"type\":\"array\",\"children\":[{\"name\":\"b\",\"className\":null,\"type\":\"integer\"}]}]}");
        String xml = "<a id=\"1\"><b id=\"2\">2</b><b id=\"2\">2</b><c>2</c></a>";
        JsonType jsonType = parseXml(xml, "{\"name\":\"a\",\"className\":null,\"attrs\":{\"id\":\"1\"},\"type\":\"object\",\"children\":[{\"name\":\"b\",\"className\":null,\"attrs\":{\"id\":\"2\"},\"type\":\"array\",\"children\":[{\"name\":\"b\",\"className\":null,\"type\":\"integer\"}]},{\"name\":\"c\",\"className\":null,\"type\":\"integer\"}]}");
        Map<String,Object> value = new HashMap<>();
        value.put("c",2);
        List b = new ArrayList<>();
        b.add(2);
        b.add(2);
        value.put("b",b);
        List<XNode> nodes = jsonType.transformToXml(value);
        String transformToXml = XNode.toXml(nodes);
        assertEquals(xml,transformToXml);
    }

    public void testFormatXml(){
        String xml = "<a id=\"1\"><b id=\"2\">2</b><b id=\"2\">2</b><c>2</c></a>";
        boolean hasXmlDeclaration = xml.indexOf("<?xml") != -1;
        Document document = XmlUtils.parseXml(xml, false);

        String result = XmlUtils.writeXml(document,hasXmlDeclaration);
        assertEquals("<a id=\"1\">\n" +
                "   <b id=\"2\">2</b>\n" +
                "   <b id=\"2\">2</b>\n" +
                "   <c>2</c>\n" +
                "</a>",result);
    }

}
