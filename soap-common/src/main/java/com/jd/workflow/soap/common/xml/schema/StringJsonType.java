package com.jd.workflow.soap.common.xml.schema;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.exception.ToXmlTransformException;
import com.jd.workflow.soap.common.mapping.impl.JsonStringParameterMapper;
import com.jd.workflow.soap.common.mapping.impl.XmlStringParameterMapper;
import com.jd.workflow.soap.common.type.JsonStringArray;
import com.jd.workflow.soap.common.type.JsonStringObject;
import com.jd.workflow.soap.common.type.XmlStringObject;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用来处理字符串string_xml以及string_json，必须由用户录入
 */
public class StringJsonType extends ComplexJsonType{
    List<JsonType> children = new ArrayList<>();
    String type;

    @Override
    public List<JsonType> getChildren() {
        return children;
    }

    @Override
    public void setChildren(List<JsonType> children) {
        this.children = children;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }

    @Override
    public Object toDescJson() {
        //Object o = getChildren().get(0).toDescJson();
        return type;
    }

/*    @Override
    public Object toJsonValue() {
        return null;
    }*/

    @Override
    public Object toExprValue(ValueBuilderAcceptor acceptor) {
        Object exprValue = getExprValue();
        JsonType jsonType = null;
        if(!getChildren().isEmpty()){
            jsonType = getChildren().get(0);
        }


        if(exprValue == null && jsonType != null){ // 当前字段的映射不为空，直接映射当前字段即可

             exprValue = jsonType.toExprValue(acceptor);
        }



        if(StringJsonOrXmlType.string_json.name().equalsIgnoreCase(getType())
        ){
            return new JsonStringParameterMapper(exprValue);
        }else{
            return new XmlStringParameterMapper(exprValue,jsonType);
        }
    }

    @Override
    public Class getTypeClass() {
        return String.class;
    }



    @Override
    public void transformToXml(XNode parent, Object inputValue, List<String> currentLevel, XmlBuilderAcceptor acceptor) throws ToXmlTransformException {

        XNode node = XNode.make(getFullTagName());
        if(acceptor != null)   acceptor.beforeBuildNode(node,this);
        this.addAttributeTo(node);

        String content = "";
        if(StringJsonOrXmlType.string_xml.name().equalsIgnoreCase(getType())
        ){
            XNode root = XNode.make("root");
            JsonType child = getChildren().get(0);
            XmlStringObject obj = (XmlStringObject) inputValue;
            Object actualValue = obj.get(child.getName());

            child.transformToXml(root, actualValue, currentLevel, acceptor);
            List<XNode> children = root.getChildren();
            content  = XNode.toXml(children);
            node.content(content);
        }else{
            node.content(toJSONString(inputValue));
        }
        parent.appendChild(node);
        if(acceptor != null) acceptor.afterBuildNode(node,this);
    }
    String toJSONString(Object inputValue){
        if(inputValue instanceof JsonStringObject){
            return JsonUtils.toJSONString(new HashMap<>((Map)inputValue));
        }else if(inputValue instanceof JsonStringArray){
            return JsonUtils.toJSONString(new ArrayList<>((List)inputValue));
        }else{
            return JsonUtils.toJSONString(inputValue);
        }
    }
    boolean isXml(){
        return StringJsonOrXmlType.string_xml.name().equals(getType());
    }
    @Override
    public JsonType newEntity() {
        StringJsonType result = new StringJsonType();
        result.setType(type);
        return result;
    }

    @Override
    public void buildExprNode(ExprTreeNode parent) {
        if(isXml()){ // xml不可以跳过子节点
            ExprTreeNode current = super.buildCurrentExprNode(parent);
            for (JsonType child : getChildren()) {
                child.buildExprNode(current);
            }
        }else{
            if(getChildren().isEmpty()){
                super.buildCurrentExprNode(parent);
                return;
            }
            // json需要跳过子节点
            JsonType childType = getChildren().get(0);
            JsonType clone = childType.clone();
            clone.setName(this.getName());
            clone.buildExprNode(parent);
        }


    }

    @Override
    public Object castValue(Object value, List<String> currentLevels) {
        if(StringJsonOrXmlType.string_xml.name().equals(type)){ // xml类型
            return JsonStringUtils.castXmlStringValue(value,getChildren().get(0));
        }else{ // json类型
             return JsonStringUtils.castJsonStringValue(value);
        }

    }
}
