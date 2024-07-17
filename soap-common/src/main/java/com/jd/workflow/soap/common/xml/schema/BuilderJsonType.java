package com.jd.workflow.soap.common.xml.schema;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.jd.workflow.soap.common.exception.ToXmlTransformException;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlbeans.impl.common.QNameHelper;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用来方便的转换为各种jsonType
 */
@Slf4j

public class BuilderJsonType extends JsonType{
    String type;
    String arrayItemType;

    String refName;

    List<BuilderJsonType> children = new ArrayList<>();

    public static BuilderJsonType fromJsonType(JsonType jsonType,boolean containsExtAttrs){
        if(jsonType == null) return null;
        BuilderJsonType builderJsonType = new BuilderJsonType();
        jsonType.cloneTo(builderJsonType);
        builderJsonType.type = jsonType.getType();
        if(!containsExtAttrs){
            builderJsonType.extAttrs.clear();
        }
        if(jsonType instanceof RefObjectJsonType){
            builderJsonType.refName = ((RefObjectJsonType) jsonType).getRefName();
        }
        List<JsonType> genericTypes = new ArrayList<>();
        if(jsonType.getGenericTypes() != null){
            for (JsonType genericType : jsonType.getGenericTypes()) {
                genericTypes.add(fromJsonType(genericType,containsExtAttrs));
            }
            builderJsonType.setGenericTypes(genericTypes);
        }
        List<BuilderJsonType> children = new ArrayList<>();
        if(jsonType instanceof ComplexJsonType){
            for (JsonType child : ((ComplexJsonType) jsonType).getChildren()) {
                children.add(fromJsonType(child,containsExtAttrs));
            }
            builderJsonType.setChildren(children);
        }
        return builderJsonType;
    }

    @Override
    public String getType() {
        return this.type;
    }
    public static BuilderJsonType from(String paramType){
        BuilderJsonType ret  = new BuilderJsonType();
       ret.type = paramType ;
       return ret;
    }
    public void setName(QName key) {
        assert  key != null;
        if(StringUtils.isNotEmpty(key.getNamespaceURI())){
            this.namespacePrefix = prefixForNamespace(key.getNamespaceURI(),null,true);
        }

        this.name = key.getLocalPart();
    }
    public void insertAttributeWithValue(QName name,String value){
        String prefix = prefixForNamespace(name.getNamespaceURI(),null,true);
        if(StringUtils.isEmpty(prefix)){
            addXmlAttr(name.getLocalPart(),value);
        }else{
            addXmlAttr(prefix+":"+name.getLocalPart(),value);
        }

    }
    public void setXsiType(String xsiType){
        addXmlAttr("xsi:type",xsiType);
    }
    public static BuilderJsonType from(SimpleParamType paramType){
        return from(paramType.typeName());
    }
    public void setType(SimpleParamType type) {
        this.type = type.typeName();
    }
    public void setType(String type) {
        this.type = type;
    }

    public String getArrayItemType() {
        return arrayItemType;
    }

    public void setArrayItemType(String arrayItemType) {
        this.arrayItemType = arrayItemType;
    }

    public void setChildren(List<BuilderJsonType> children) {
        this.children = children;
    }

    @Override
    public boolean isSimpleType() {
        for (SimpleParamType value : SimpleParamType.values()) {
            if(value.typeName().equals(type)){
                return true;
            }
        }
        return false;
    }




    public BuilderJsonType createChild(){
        return createChild(null);
    }
    public BuilderJsonType createChild(String type){
        return createChild(type,false);
    }
    public BuilderJsonType createChild(String type,boolean first){
        BuilderJsonType child = BuilderJsonType.from(type);
        addChild(child,first);
        return child;
    }
    public void addChild(BuilderJsonType child,boolean first){
        child.setParent(this);
        if(first){
            children.add(0,child);
        }else{
            children.add(child);
        }

    }

    public void addChild(BuilderJsonType child){
         addChild(child,false);
    }

    public List<BuilderJsonType> getChildren() {
        return children;
    }


    @Override
    public Object toDescJson() {
        return toJsonType().toDescJson();
    }



    @Override
    public Object toExprValue(ValueBuilderAcceptor acceptor) {
        return toJsonType().toExprValue(acceptor);
    }

    @Override
    public Class getTypeClass() {
        return toJsonType().getTypeClass();
    }

    @Override
    public Map<String, Object> toJson() {
        return toJsonType().toJson();
    }

    @Override
    public JsonType newEntity() {
        return new BuilderJsonType();
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

    @Override
    public void transformToXml(XNode parent, Object inputValue, List<String> currentLevel,XmlBuilderAcceptor acceptor) throws ToXmlTransformException {
        toJsonType().transformToXml(parent, inputValue, currentLevel, acceptor);
    }



    @Override
    public String toString() {
        return "BuilderJsonType{" +
                "name='" + name + '\'' +
                '}';
    }
    private void initJsonType(JsonType jsonType){
        /*jsonType.setName(this.getName());
        jsonType.setRawName(this.getRawName());
        jsonType.setNamespacePrefix(namespacePrefix);
        jsonType.setDesc(getDesc());
        jsonType.setValue(this.value);
        jsonType.setCompiledValue(this.value);
        jsonType.setClassName(this.className);
        jsonType.setExprType(exprType);
        jsonType.setExtAttrs(this.extAttrs);
        jsonType.setRequired(required);
        for (Map.Entry<String, String> entry : getAttrs().entrySet()) {
            jsonType.addXmlAttr(entry.getKey(),entry.getValue());
        }*/
        super.cloneTo(jsonType);
        if(this.genericTypes != null){
            jsonType.genericTypes = new ArrayList<>();
            for (JsonType genericType : this.genericTypes) {
                jsonType.genericTypes.add(((BuilderJsonType)genericType).toJsonType());
            }

        }
    }
    public JsonType toJsonType(){
        if("array".equals(getType()) ){
            ArrayJsonType arrayJsonType = new ArrayJsonType();
            initJsonType(arrayJsonType);

            if(StringUtils.isNotBlank(getArrayItemType())){
                if("array".equals(getArrayItemType())){
                    //arrayJsonType.setArrayItemType(getArrayItemType());
                    for (BuilderJsonType child : children) {
                        arrayJsonType.addChild(child.toJsonType());
                    }
                    return arrayJsonType;
                }else if("object".equals(getArrayItemType())){
                    ObjectJsonType objectJsonType = new ObjectJsonType();
                    initJsonType(objectJsonType);
                    arrayJsonType.addChild(objectJsonType);
                    for (BuilderJsonType child : children) {
                        objectJsonType.addChild(child.toJsonType());
                    }
                }else {
                    SimpleJsonType jsonType = new SimpleJsonType();
                    initJsonType(jsonType);
                    jsonType.setType(getArrayItemType());
                    arrayJsonType.addChild(jsonType);

                }
            }else{
                for (BuilderJsonType child : children) {
                    arrayJsonType.addChild(child.toJsonType());
                }
            }


            return arrayJsonType;


        }else if("string_json".equals(getType()) || "string_xml".equals(getType())){
            StringJsonType stringJsonType = new StringJsonType();
            stringJsonType.setType(getType());
            initJsonType(stringJsonType);
            for (BuilderJsonType child : children) {
                stringJsonType.getChildren().add(child.toJsonType());
            }
            return stringJsonType;
        }else if("object".equals(getType()) || "map".equals(getType())){
            ObjectJsonType objectJsonType = new ObjectJsonType();
            if("map".equals(getType())){
                objectJsonType = new MapJsonType();
            }
            initJsonType(objectJsonType);
            for (BuilderJsonType child : children) {
                objectJsonType.addChild(child.toJsonType());
            }
            return objectJsonType;
        }else if("ref".equals(getType())){
            RefObjectJsonType refJsonType = new RefObjectJsonType();
            initJsonType(refJsonType);
            refJsonType.setRefName(getRefName());
            for (BuilderJsonType child : children) {
                refJsonType.addChild(child.toJsonType());
            }
            return refJsonType;
        }else{
            SimpleJsonType simpleJsonType = new SimpleJsonType();
            initJsonType(simpleJsonType);
            String type = getType();
            if(StringUtils.isEmpty(type)){
                type = "string";
            }
            simpleJsonType.setType(type);
            return simpleJsonType;
        }
    }
    public String prefixForNamespace(String ns, String suggestion, boolean createIfMissing) {
        if (ns == null) {
            ns = "";
        }

        if (ns.equals("http://www.w3.org/XML/1998/namespace")) {
            return "xml";
        } else if (ns.equals("http://www.w3.org/2000/xmlns/")) {
            return "xmlns";
        } else {
            BuilderJsonType base;
            for(base = this; base.getParent()!=null; base = base.getParent()) {
            }

            String c;
            if (ns.length() == 0) {
                c = base.findXmlnsForPrefix("");
                if (c != null && c.length() != 0) {
                    if (!createIfMissing) {
                        return null;
                    } else {
                        //base.setAttr(this._locale.createXmlns((String)null), "");
                        return "";
                    }
                } else {
                    return "";
                }
            } else {
                String prefix = base.getXmlnsPrefixForUri(ns) ;
                if(prefix != null){
                    return prefix;
                }


                if (!createIfMissing) {
                    return null;
                } else {
                    if (suggestion != null && (suggestion.length() == 0 ||
                            suggestion.toLowerCase().startsWith("xml") ||
                            base.findXmlnsForPrefix(suggestion) != null)) {
                        suggestion = null;
                    }

                    if (suggestion == null) {
                        String prefixBase = QNameHelper.suggestPrefix(ns);
                        suggestion = prefixBase;

                        for(int i = 1; base.findXmlnsForPrefix(suggestion) != null; suggestion = prefixBase + i++) {
                        }
                    }

                 /*   for(c = base; !c.isRoot() && !c.ensureParent().isRoot(); c = c._parent) {
                    }*/

                    base.addXmlAttr("xmlns:"+suggestion, ns);
                    return suggestion;
                }
            }
        }
    }

    @Override
    public void cloneTo(JsonType jsonType) {
        toJsonType().cloneTo(jsonType);
    }

    @Override
    public void buildExprNode(ExprTreeNode parent) {
        toJsonType().buildExprNode(parent);
    }

    @Override
    public Object castValue(Object value, List<String> currentLevels) {
        return toJsonType().castValue(value,currentLevels);
    }
}
