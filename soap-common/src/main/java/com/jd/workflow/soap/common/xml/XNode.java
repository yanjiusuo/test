package com.jd.workflow.soap.common.xml;

import com.jd.workflow.soap.common.enums.ExprType;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.jd.workflow.soap.common.xml.schema.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.*;

import static com.jd.workflow.soap.common.util.XmlUtils.isEmptyNode;
import static com.jd.workflow.soap.common.util.XmlUtils.parseXml;

public class XNode {
    static final Logger LOG = LoggerFactory.getLogger(XNode.class);
    private static final long serialVersionUID = -8460236455991070110L;


    private String tagName;
    private List<XNode> children = Collections.emptyList();
    private Map<String, String> attributes = Collections.emptyMap();
    private XNode parent;
    String content;

    private int flags;
    /**
     *  有些额外的属性可能需要在这里存放，与构造xml无关
     */
    Map<String,Object> extAttrs = Collections.emptyMap();
    private String comment;
    private String afterComment;
    /**
     * id属性
     */
    private String uniqueAttr;

    protected XNode(String tagName) {
        this.content = "";
        this.tagName = tagName;
    }

    public static XNode make(String tagName) {
        assert  tagName != null;
        return new XNode(tagName);
    }



    public static XNode makeTextNode() {
        XNode node = new XNode("#text");
        return node;
    }

    public static XNode makeDummyNode() {
        return new XNode("_");
    }

    public static XNode makeTextNode(String value) {
        return makeTextNode().content(value);
    }

    public void _assignAttributes(Map<String, String> attrs) {


        if (attrs == null) {
            attrs = Collections.emptyMap();
        }

        this.attributes = attrs;
    }





    public boolean isReadOnly() {
        return (this.flags & 8) != 0;
    }



    public String comment() {
        return this.comment;
    }

    public XNode comment(String comment) {


        this.comment = comment;
        return this;
    }

    public String afterComment() {
        return this.afterComment;
    }

    public XNode afterComment(String comment) {


        this.afterComment = comment;
        return this;
    }

    public boolean isTextNode() {
        return this.tagName.equals("#text");
    }

    public boolean isElementNode() {
        return !this.isTextNode();
    }

    public String content() {
        return this.content;
    }

    public XNode contentAsNode() {
        XNode node = make("#text");
        node.flags = this.flags & 1;
        node.content(this.content());
        return node;
    }

    public Object value() {
        return this.content;
    }

    public XNode content(String value) {
        this.content = value;
        return this;
    }




    public XNode value(String value) {
        return this.content(value);
    }


    @Override
    public String toString() {
        return "XNode{" +
                "tagName='" + tagName + '\'' +
                '}';
    }

    public String uniqueAttr() {
        return this.uniqueAttr;
    }

    public XNode uniqueAttr(String idAttr) {

        this.uniqueAttr = idAttr;
        return this;
    }



    public boolean isMarkedList() {
        return (this.flags & 4) != 0;
    }

    public XNode markList(boolean markList) {
        if (markList) {
            this.flags |= 4;
        } else {
            this.flags &= -5;
        }

        return this;
    }

    public boolean isMarkedCDATA() {
        return (this.flags & 1) != 0;
    }

    public boolean isMarkedFullTagEnd() {
        return (this.flags & 2) != 0;
    }

    public XNode markCDATA(boolean cdata) {
        if (cdata) {
            this.flags |= 1;
        } else {
            this.flags &= -2;
        }

        return this;
    }

    public XNode markFullTagEnd(boolean fullTagEnd) {
        if (fullTagEnd) {
            this.flags |= 2;
        } else {
            this.flags &= -3;
        }

        return this;
    }

    public String tagName() {
        return this.tagName;
    }

    public XNode tagName(String tagName) {
        this.tagName = tagName;
        return this;
    }

    public String tagNameWithoutNs() {
        int idx = this.tagName.indexOf(58);
        return idx < 0 ? this.tagName : this.tagName.substring(idx + 1);
    }

    public String tagNameNsPrefix() {
        int idx = this.tagName.indexOf(58);
        return idx < 0 ? "" : this.tagName.substring(0, idx + 1);
    }

    public String tagNameNs() {
        int idx = this.tagName.indexOf(58);
        return idx < 0 ? null : this.tagName.substring(0, idx);
    }

    public boolean hasTagNameNs() {
        return this.tagName.indexOf(58) > 0;
    }
    String toNullString(Object o){
        if(o == null){
            return "";
        }
        return o.toString();
    }
    public String id() {
        return toNullString( this.attr("id"));
    }

    public XNode parent() {
        return this.parent;
    }

    public XNode getParent() {
        return this.parent;
    }



    public XNode detach() {


        if (this.parent != null) {
            this.parent.children().remove(this);
            this.parent = null;
        }

        return this;
    }



    public Map<String, String> attributes() {
        return this.attributes;
    }



    public Set<String> attrNames() {
        return this.attributes.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(this.attributes.keySet());
    }

    public List<String> childNames() {
        if (!this.hasChild()) {
            return Collections.emptyList();
        } else {
            ArrayList list = new ArrayList(this.childCount());
            Iterator child = this.children.iterator();

            while(child.hasNext()) {
                XNode next = (XNode)child.next();
                list.add(next.tagName());
            }

            return list;
        }
    }

    public Object attr(String name) {
        return this.attributes.get(name);
    }



    public XNode attr(String name, String value) {
        if(value == null) value = "";

        if (this.attributes.equals(Collections.emptyMap())) {
            this.attributes = new LinkedHashMap();
        }

        this.attributes.put(name, value);
        return this;
    }



    public XNode attrs(Map<String, String> attrs) {


        if (attrs != null) {
            if (this.attributes.equals(Collections.emptyMap())) {
                this.attributes = new LinkedHashMap();
            }
            this.attributes.putAll(attrs);
        }

        return this;
    }

    public XNode attrValues(Map<String, String> attrs) {


        if (attrs != null) {
            if (Collections.emptyMap().equals(this.attributes)) {
                this.attributes = new LinkedHashMap();
            }

            this.attributes.putAll(attrs);
        }

        return this;
    }

    public XNode attrValue(String name, String value) {

        if (this.attributes.equals(Collections.emptyMap()) ) {
            this.attributes = new LinkedHashMap();
        }

        this.attributes.put(name, value);
        return this;
    }

    public boolean hasAttr() {
        return !this.attributes.isEmpty();
    }

    public boolean hasAttr(String attrName) {
        return this.attributes.containsKey(attrName);
    }

    public String removeAttr(String name) {


        String value = this.attributes.remove(name);
        return value == null ? null : value;
    }

    public boolean removeAttrs(Collection<String> keys) {


        return keys != null ? this.attributes.keySet().removeAll(keys) : false;
    }




    public final boolean hasChild() {
        return !this.children.isEmpty();
    }

    public final boolean hasBody() {
        return this.hasChild() || !"".equals(this.content);
    }

    public boolean hasChild(String tagName) {
        return this.childByTag(tagName) != null;
    }

    public final int childCount() {
        return this.children.size();
    }

    public final List<XNode> children() {
        return this.children;
    }

    public final List<XNode> getChildren() {
        return this.children;
    }

    public final XNode child(int index) {
        return (XNode)this.children.get(index);
    }



    public XNode childByTag(String tagName) {
        if (this.children.isEmpty()) {
            return null;
        } else {
            Iterator iter = this.children.iterator();

            XNode next;
            do {
                if (!iter.hasNext()) {
                    return null;
                }

                next = (XNode)iter.next();
            } while(!next.tagName().equals(tagName));

            return next;
        }
    }



    public List<XNode> parents() {
        ArrayList ret = new ArrayList();
        XNode node = this;

        while(true) {
            XNode parent = node.parent();
            if (parent == null) {
                Collections.reverse(ret);
                return ret;
            }

            ret.add(parent);
            node = parent;
        }
    }

    public XNode closest(String name) {
        XNode node = this;

        while(!node.tagName().equals(name)) {
            node = node.parent();
            if (node == null) {
                return null;
            }
        }

        return node;
    }



    public XNode root() {
        XNode node = this;

        while(true) {
            XNode parent = node.parent();
            if (parent == null) {
                return node;
            }

            node = parent;
        }
    }

    public XNode getRoot() {
        return this.root();
    }

    public XNode firstChild() {
        return this.children.isEmpty() ? null : (XNode)this.children.get(0);
    }

    public XNode lastChild() {
        return this.children.isEmpty() ? null : (XNode)this.children.get(this.children.size() - 1);
    }

    public int childIndex() {
        return this.parent == null ? -1 : this.parent.children().indexOf(this);
    }

    public int depth() {
        if (this.parent == null) {
            return 1;
        } else {
            int depth = 1;
            XNode parent = this.parent;

            do {
                ++depth;
                parent = parent.parent;
            } while(parent != null);

            return depth;
        }
    }


    public XNode nextSibling() {
        if (this.parent == null) {
            return null;
        } else {
            int index = this.parent.children().indexOf(this);
            if (index < 0) {
                return null;
            } else {
                ++index;
                return index >= this.parent.children().size() ? null : (XNode)this.parent.children().get(index);
            }
        }
    }


    private void ensureChildValid(){
        if(Collections.emptyList().equals(this.children)){
            this.children = new ArrayList<>();
        }
    }
    public XNode makeChild(String tagName) {
        XNode child = this.childByTag(tagName);
        if (child == null) {
            child = make(tagName);
            this.appendChild(child);
        }

        return child;
    }

    public XNode addChild(String tagName) {
        XNode child = make(tagName);

        this.appendChild(child);
        return child;
    }
    public XNode appendChildren(String text){
        List<Node> nodes = XmlUtils.parseXmlFragment(text);
        List<XNode> children = new ArrayList<>();
        for (Node node : nodes) {
            XNode child = toXNode(node);
            children.add(child);
        }
        appendChildren(children);
        return this;
    }
    static List<Node> filterInvalidChild(NodeList nodeList){
        List<Node> list = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node item = nodeList.item(i);
            if(XmlUtils.isEmptyNode(item) || XmlUtils.isComment(item)){
                continue;
            }
            list.add(item);
        }
        return list;
    }
   static XNode toXNode(Node node) {
        XNode xNode = XNode.make(node.getNodeName());
       List<Node> childNodes = filterInvalidChild(node.getChildNodes());
       for (int i = 0; i < node.getAttributes().getLength(); i++) {
           Node attr = node.getAttributes().item(i);
           xNode.attr(attr.getNodeName(),attr.getNodeValue());
       }
        if (childNodes.size() == 1 && childNodes.get(0).getNodeValue() != null) {
            return xNode.content(childNodes.get(0).getNodeValue());
        }

       for (Node child : childNodes) {

           if (child.getNodeType() == Node.ELEMENT_NODE) {
               XNode childNode = toXNode(node);

               xNode.appendChild(childNode);

           }
       }

        return xNode;
    }
    public XNode appendChildren(Collection<XNode> children) {
        for (XNode child : children) {
            appendChild(child);
        }
        return this;
    }
    public XNode appendChild(XNode child) {
        ensureChildValid();
        this.children.add(child);
        return this;
    }

    public XNode prependChild(XNode child) {

        ensureChildValid();
        this.children.add(0, child);
        return this;
    }

    public XNode insertChild(int index, XNode child) {

        ensureChildValid();

        if (index <= 0) {
            return this.prependChild(child);
        } else if (index >= this.childCount()) {
            return this.appendChild(child);
        } else {
            this.children.add(index, child);
            return this;
        }
    }


    public XNode clearAttrs() {
        if (this.attributes.equals(Collections.emptyMap())) {
            this.attributes.clear();
        }

        return this;
    }

    public XNode clearChildren() {
        this.children = Collections.emptyList();
        return this;
    }


    public XNode clearBody() {
        this.content = "";
        this.children = Collections.emptyList();
        return this;
    }

    public boolean hasContent() {
        return !"".equals(this.content);
    }

    private static String getNamespaceUri(XNode node,String prefix){
        String xmlAttr = "xmlns";
        if(!StringUtils.isEmpty(prefix)){
            xmlAttr = xmlAttr+":"+prefix;
        }
        String namespaceURI = (String) node.attr(xmlAttr);

        if(StringUtils.isNotBlank(namespaceURI)){
            return namespaceURI;
        }else if(node.parent() != null){
            return getNamespaceUri(node.parent(),prefix);
        }
        return null;
    }
    public List<XNode> childrenByTag(String tagName) {
        if (this.children.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<XNode> nodes = new ArrayList<>();
            Iterator iter = this.children.iterator();

            XNode next;
            do {

                next = (XNode)iter.next();
                if(next.tagName().equals(tagName)){
                    nodes.add(next);
                }
            } while(iter.hasNext());

            return nodes;
        }
    }
    static Element toElement(Document document,XNode node){
        assert node.tagName != null;

      Element elm = null;;


      String namespaceURI = getNamespaceUri(node,node.tagNameNs());

      if(StringUtils.isNotBlank(namespaceURI)){
          elm = document.createElementNS(namespaceURI,node.tagName);
      }else {
          elm = document.createElement(node.tagName);
      }



       for (Map.Entry<String, String> entry : node.attributes().entrySet()) {
           elm.setAttribute(entry.getKey(),entry.getValue());

       }
       if(node.hasContent()){

               elm.appendChild(document.createTextNode(node.content()));//document.createCDATASection()



       }
      if(node.hasChild()){
          for (XNode child : node.getChildren()) {
              child.parent = node;
              elm.appendChild(toElement(document,child));
          }
      }
      document.appendChild(elm);
          return elm;
   }
   public static XNode parse(String xml){
        return XmlUtils.parseXmlToXNode(xml);
   }
    public JsonType toJsonType(String ...arrayJsonPath){
        return toJsonType(Arrays.asList(arrayJsonPath),"/"+tagName,false);
    }
    private JsonType toJsonType(List<String> arrayJsonPath, String currentPath, boolean isArrayPath){
        JsonType jsonType = null;
        if(arrayJsonPath.contains(currentPath) || isArrayPath){
            ArrayJsonType arrayJsonType = new ArrayJsonType();
            for (Map.Entry<String, String> entry : attributes.entrySet()) {
                arrayJsonType.addXmlAttr(entry.getKey(),entry.getValue());
            }
            arrayJsonType.setName(tagNameWithoutNs());
            arrayJsonType.setNamespacePrefix(tagNameNsPrefix());
            arrayJsonPath.remove(currentPath);

            XNode child = XNode.make(tagName);
            child.appendChildren(getChildren());
            child.content(this.content);
            arrayJsonType.addChild(child.toJsonType(arrayJsonPath,currentPath,false));
            return arrayJsonType;

        }else if(children.isEmpty()){
            SimpleJsonType simpleJsonType = new SimpleJsonType();
            if(StringHelper.isNumber(content)){
                if(content.indexOf(".") == -1){
                    simpleJsonType.setType(SimpleParamType.INTEGER.typeName());
                }else{
                    simpleJsonType.setType(SimpleParamType.DOUBLE.typeName());
                }
            }else{
                simpleJsonType.setType(SimpleParamType.STRING.typeName());
            }
            jsonType = simpleJsonType;
        }else {
            jsonType = new ObjectJsonType();
        }
        jsonType.setExprType(ExprType.expr);
        jsonType.setName(tagNameWithoutNs());
        jsonType.setNamespacePrefix(tagNameNsPrefix());
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            jsonType.addXmlAttr(entry.getKey(),entry.getValue());
        }
        //jsonType.getAttrs().putAll();

        if(jsonType instanceof ComplexJsonType){
            List<JsonType> childJsonTypes = new ArrayList<>();
            ((ComplexJsonType) jsonType).setChildren(childJsonTypes);
            List<String> processed = new ArrayList<>();
            for (XNode child : getChildren()) {
                if(processed.contains(child.tagName())){
                    continue;
                }
                processed.add(child.tagName());
                String childPath = currentPath+"/"+child.tagName();
                List<XNode> sameTagChilds = childrenByTag(child.tagName());
                if(sameTagChilds.size()>1){
                    childJsonTypes.add(child.toJsonType(arrayJsonPath,childPath,true));
                }else{
                    childJsonTypes.add(child.toJsonType(arrayJsonPath,childPath,false));
                }
            }

        }
        return jsonType;
    }

    public String toXml(){
        return toXml(true,true);
    }

    public static String toXml(List<XNode> nodes){
        String result = "";
        for (XNode node : nodes) {
            result+=node.toXml(false,false);
        }
        return result;
    }
    public String toXml(boolean xmlDeclation,boolean indent){
        try {
            DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();//解析器55
            factory.setExpandEntityReferences(false);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setXIncludeAware(false);

            DocumentBuilder builder= null;//操作的Document对象58
            builder = factory.newDocumentBuilder();
            Document document=builder.newDocument();
            Element element = toElement(document,this);

            return XmlUtils.writeXml(document,xmlDeclation,indent);
        } catch (ParserConfigurationException e) {
            throw new StdException("xml.err_build_xml",e);
        }
    }

    public static void main(String[] args) {
        String pom = "<dependency><groupId>com.jd.workflow</groupId><artifactId>springmvc-demo-for-test</artifactId><version>1.0.0-SNAPSHOT</version></dependency>";
        XNode node = parse(pom);
        System.out.println(node);
    }



}