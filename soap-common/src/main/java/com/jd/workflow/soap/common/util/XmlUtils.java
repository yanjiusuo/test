package com.jd.workflow.soap.common.util;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

public class XmlUtils {
    public static final String TEXT_CONTENT_NAME = "#text";
    static final Logger logger = LoggerFactory.getLogger(XmlUtils.class);
    public static List<Node> parseXmlFragment(String content){
        if(StringUtils.isBlank(content)) return Collections.emptyList();

        content = content.trim();
        Document document = null;
        if(content.indexOf("<?xml") != -1){
             document = parseXml(content, false);
        }else{
            String xmlStr = "<root>"+content.trim()+"</root>";
             document = parseXml(xmlStr, false);
        }


        NodeList childNodes = document.getDocumentElement().getChildNodes();
        List<Node> children = new ArrayList<>();
        for (int i = 0; i < childNodes.getLength(); i++) {
            children.add(childNodes.item(i));
        }
        return children;
    }
    public static Document parseXml(String xmlStr,boolean useNamespaceAware) {
        Document document = null;
        try {
            DocumentBuilder builder = createDocumentBuilder(useNamespaceAware);
            document = builder.parse(new InputSource(new StringReader(xmlStr)));
            //document.getDocumentElement().getNamespaceURI()
        } catch (Exception e) {
            logger.error("xml.err_parse_xml:xml={}",xmlStr,e);
            throw new StdException("xml.err_parse_xml:xml="+xmlStr,e);
        }
        return document;
    }
    public static Document parseXml(String xmlStr) {
        return parseXml(xmlStr,true);
    }
    public static String writeXml(Document document,boolean useXmlDeclation){
        return writeXml(document,useXmlDeclation,true);
    }
    public static String writeXml(Document document){
        return writeXml(document,true,true);
    }
    public static String writeXml(Document document,boolean useXmlDeclation,boolean indent){
        TransformerFactory transFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            StringWriter writer = new StringWriter();
            transformer = transFactory.newTransformer();
            if(indent){
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            }else{
                transformer.setOutputProperty(OutputKeys.INDENT, "no");
            }
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");

            if(useXmlDeclation){

                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"no"); // omit_xml_declaration 表示是否删除掉xml声明
            }else{
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION,"yes");
            }

            DOMSource source = new DOMSource();
            source.setNode(document);
            StreamResult result = new StreamResult();
            result.setWriter(writer);
            transformer.transform(source, result);
            return writer.toString();
        } catch (Exception e) {
            logger.error("xml.err_write_xml",e);
            throw StdException.adapt("xml.err_write_xml",e);
        }
    }

    private static DocumentBuilder createDocumentBuilder(boolean namespaceAware){
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(namespaceAware);
            dbf.setExpandEntityReferences(false);
            dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            dbf.setXIncludeAware(false);
            return dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解析xml或者解析xml 片段，也就意味着要支持：
     *  全量xml: <?xml version="1.0" encoding="UTF-8"?><a>123</a>
     *  或者局部的xml片段，比如<a>1</a><b>2</b>
     * @param xml
     * @param arrayPaths
     * @return
     */
    public static Object parseXmlPart(String xml,boolean ignoreAttr,String... arrayPaths){
        if(StringUtils.isBlank(xml)) return null;
        xml = xml.trim();
        if(xml.indexOf("<?xml") != -1){
            return xmlToMap(xml,arrayPaths);
        }
        String newXml = "<root>" + xml +"</root>";
        List<String> paths = new ArrayList<>(arrayPaths.length);
        for (String arrayPath : arrayPaths) {
            paths.add("/root"+arrayPath);
        }
        Map map = xmlToMap(newXml, ignoreAttr, paths.toArray(new String[0]));
        return map.get("root");
    }

    public static Map xmlToMap(String xml,String ...arrayPaths){
        return xmlToMap(xml,false,arrayPaths);
    }
    public static Map xmlToMap(String xml,boolean ignoreAttr,String ...arrayPaths){
        if(StringUtils.isEmpty(xml)) return new HashMap();
        Document document = parseXml(xml,false);
        return elementToValue(document.getDocumentElement(),Arrays.asList(arrayPaths),ignoreAttr);
    }

    /**
     * 将xml解析成XNode节点
     * @param xml
     * @return
     */
    public static XNode parseXmlToXNode(String xml){
        if(StringUtils.isEmpty(xml)) return null;
        Document document = parseXml(xml,false);
        return elementToXNode(document.getDocumentElement());
    }



    private static XNode elementToXNode(Node element){

        XNode node = XNode.make(element.getNodeName());
        boolean isTextNode = isOnlyTextContentNode(element);
        Map<String,String> attributes = getAttrs(element.getAttributes());
        node.attrs(attributes);
        if(isTextNode){ // 文本节点
            String nodeValue = StringUtils.strip(element.getTextContent());
            node.content(nodeValue);
            return node;
        }

        Map<String,Object> result = new LinkedHashMap<>();
        result.putAll(attributes);

        List<Node> children = nodeListToListNode(element.getChildNodes());
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);

            XNode childNode = elementToXNode(child);
            node.appendChild(childNode);

        }
        return node;

    }


    public static Map nodeToMap(Node node,String ...arrayPaths){
        return nodeToMap(node,false,arrayPaths);
    }

    public static Map nodeToMap(Node node,boolean ignoreAttr,String ...arrayPaths){
        if(node == null) return new HashMap();
        return elementToValue(node,Arrays.asList(arrayPaths),ignoreAttr);
    }


    public static Object select(String xml, String expression, QName typeName,String ...arrayPaths) throws XPathExpressionException {
        Document document = parseXml(xml);


        // 生成XPath对象
        XPath xpath = XPathFactory.newInstance().newXPath();
        Object evaluate = xpath.evaluate(expression, document, typeName);
        if(evaluate == null){
            return null;
        }
        if(evaluate instanceof Node){
            return elementToValue((Node)evaluate,Arrays.asList(arrayPaths),false);
        }else if(evaluate instanceof NodeList){
            return nodeListValue((NodeList)evaluate,"",Arrays.asList(arrayPaths));
        }
        return evaluate;
    }



    private static Object formatXmlNodeValue(String value){
        return value;
    }
    private static List nodeListValue(NodeList nodeList, String currentPath, List<String> arrayPaths){
        List<Node> elements = nodeListToListNode(nodeList);

        return nodeListValue(elements,currentPath,arrayPaths,false);
    }

    /**
     * 忽略掉nodelist里的注释、空节点
     * @param nodeList
     * @return
     */
    static List<Node> nodeListToListNode(NodeList nodeList){
        List<Node> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if(isEmptyNode(item) || isComment(item)){
                continue;
            }
            list.add(item);
        }
        return list;
    }
    private static List nodeListValue(List<Node> elements, String currentPath, List<String> arrayPaths,boolean ignoreAttr){
        List result = new ArrayList<>();
        boolean textContentForceUseMapType = false;
        for (Node node : elements) {
            Map<String, String> attrs = getNonNsAttrs(node.getAttributes());
            if(!attrs.isEmpty()){
                textContentForceUseMapType = true;
            }
        }
        for (Node node : elements) {
            result.add(nodeValue(node,currentPath,arrayPaths,textContentForceUseMapType,ignoreAttr));
        }
        return result;
    }
    public static boolean isComment(Node element){
        return element instanceof Comment;
    }
    static boolean isOnlyTextContentNode(Node element){
        List<Node> childNodes = nodeListToListNode(element.getChildNodes());
        return childNodes.size() == 0 || childNodes.size() == 1 && childNodes.get(0) instanceof Text;

    }

    private static String localTagName(String tagName) {
        if(tagName == null) return null;
        int idx = tagName.indexOf(":");
        return idx < 0 ? tagName : tagName.substring(idx+1);
    }

    /**
     *
     * @param element
     * @param currentPath
     * @param arrayPaths
     * @param textContentForceUseMapType 当前节点属于数组节点时， 比如<a id="1">4<a/><a>213</a> ,需要保持结构的一致性，必须保证返回的值都是map类型
     * @return
     */
    private static Object nodeValue(Node element,String currentPath, List<String> arrayPaths,boolean textContentForceUseMapType,boolean ignoreAttr){

        boolean isTextNode = isOnlyTextContentNode(element);
        Map<String,String> attributes = getNonNsAttrs(element.getAttributes());
        if(ignoreAttr){
            attributes = new HashMap<>();
        }

        if(isTextNode){ // 文本节点
            String nodeValue = StringUtils.strip(element.getTextContent());
            if(attributes.isEmpty() && !textContentForceUseMapType){
                return formatXmlNodeValue(nodeValue);
            }else{
                attributes.put(TEXT_CONTENT_NAME,(String)formatXmlNodeValue(nodeValue));
                return attributes;
            }
        }

        Map<String,Object> result = new LinkedHashMap<>();
        result.putAll(attributes);

        String tagName = localTagName(element.getNodeName());

        Set<String> processed = new HashSet<>();
        List<Node> children = nodeListToListNode(element.getChildNodes());
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            String childTagName = localTagName(child.getNodeName());
            if( processed.contains(childTagName)
                    || isEmptyNode(child)
            ){
                continue;
            }
            processed.add(childTagName);

            List<Node> childNodes = childByTag(element, child.getNodeName());
            String childPath = currentPath+"/"+childTagName;
            if(childNodes.size() > 1 ||  arrayPaths.contains(childPath)){
                result.put(childTagName, nodeListValue(childNodes,childPath,arrayPaths,ignoreAttr));
            }else if(childNodes.size() == 1){
                result.put(childTagName, nodeValue(childNodes.get(0),childPath,arrayPaths,false,ignoreAttr));
            }

        }
        return result;


    }
    private static Map elementToValue(Node element, List<String> arrayPaths,boolean ignoreAttr){
        Map<String,Object> result = new LinkedHashMap<>();
        String tagName = localTagName(element.getNodeName());
        result.put(tagName,nodeValue(element,"/"+tagName,arrayPaths,false,ignoreAttr));
        return result;
    }
    private static boolean hasSimpleAttr(NamedNodeMap attributes) {
         return !getNonNsAttrs(attributes).isEmpty();
    }
    private static Map<String,String> getNonNsAttrs(NamedNodeMap attributes) {
        Map<String,String> map = new LinkedHashMap<>();
        if(attributes == null) return map;
        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.item(i).getNodeName();

            if( !("xmlns".equals(name) || name.startsWith("xmlns:") )) {
                map.put(localTagName(name),attributes.item(i).getNodeValue());
            }
        }
        return map;
    }
    private static Map<String,String> getAttrs(NamedNodeMap attributes) {
        Map<String,String> map = new LinkedHashMap<>();
        if(attributes == null) return map;
        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.item(i).getNodeName();
             map.put(name,attributes.item(i).getNodeValue());
        }
        return map;
    }
    public static boolean isEmptyNode(Node item){
        if(StringUtils.isEmpty(item.getNodeName())) return true;
        if( TEXT_CONTENT_NAME.equals(item.getNodeName()) && StringUtils.isBlank(item.getTextContent())){
            return true;
        }
        return false;
    }

    private static List<Node> childByTag(Node node,String tagName){
        List<Node> children = new ArrayList<>();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item =  node.getChildNodes().item(i);
            if(isEmptyNode(item)) continue;

            if(tagName.equals(item.getNodeName())
            ){
                children.add(item);
            }
        }
        return children;
    }

}
