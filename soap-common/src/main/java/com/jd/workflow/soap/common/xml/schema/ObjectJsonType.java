package com.jd.workflow.soap.common.xml.schema;


import com.jd.workflow.soap.common.exception.ToXmlTransformException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
public class ObjectJsonType extends ComplexJsonType {
    List<JsonType> children = new LinkedList<>();

    @Override
    public String getType() {
        return "object";
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }


    @Override
    public Object toDescJson() {
        Map<String,Object> current = new LinkedHashMap<>();
        for (JsonType child : children) {
            current.put(child.getName(),child.toDescJson());
        }
        return current;
    }

    /*@Override
    public Object toJsonValue() {
        if(this.value != null){
            return this.value;
        }
        Map<String,Object> current = new LinkedHashMap<>();
        for (JsonType child : children) {
            current.put(child.getName(),child.toJsonValue());
        }
        return current;
    }*/

    @Override
    public Object toExprValue(ValueBuilderAcceptor acceptor) {
        Object value = this.getExprValue();
        if(value == null){
            Map<String,Object> current = new LinkedHashMap<>();
            for (JsonType child : children) {
                current.put(child.getName(),child.toExprValue(acceptor));
            }
            value = current;
        }

        if(acceptor != null) return acceptor.afterSetValue(value,this);
        return value;
    }

    @Override
    public Class getTypeClass() {
        return Map.class;
    }


    public void addChild(JsonType jsonType){
        children.add(jsonType);
    }



    @Override
    public void transformToXml(XNode parent, Object inputValue, List<String> currentLevel,XmlBuilderAcceptor acceptor) throws ToXmlTransformException {

        XNode root = XNode.make(getFullTagName());
        if(acceptor != null) acceptor.beforeBuildNode(root,this);
        //currentLevel.add(this.getName());
        if(inputValue == null ){
            if(required){
                throw new ToXmlTransformException("xml.err_miss_required_value")
                        .param("type",getType())
                        .param("level", StringUtils.join(currentLevel,">"));
            }
            return;
        }else{
            if(!(inputValue instanceof Map)){
                throw new ToXmlTransformException("xml.err_convert_type")
                        .param("expected",getType())
                        .param("actual",inputValue.getClass().getName())
                        .param("value",inputValue)
                        .param("level", StringUtils.join(currentLevel,">"));
            }
            for (JsonType child : children) {
                currentLevel.add(child.getName());
                Object o = ((Map<?, ?>) inputValue).get(child.getName());
                child.transformToXml(root,o,currentLevel,acceptor);
                currentLevel.remove(currentLevel.size()-1);
            }
        }
        addAttributeTo(root);
        //currentLevel.remove(currentLevel.size()-1);
        if(acceptor != null)  acceptor.afterBuildNode(root,this);
        parent.appendChild(root);
    }



    @Override
    public void buildExprNode(ExprTreeNode parent) {

        ExprTreeNode current = super.buildCurrentExprNode(parent);
        for (JsonType child : getChildren()) {
            child.buildExprNode(current);
        }
    }

    @Override
    public Object castValue(Object value, List<String> currentLevels) {
        if(value == null ) return value;
        if(value instanceof String && ((String) value).trim().startsWith("{")){ // 认为是map值
            value = JsonUtils.parse((String)value,Map.class);
        }else if(!(value instanceof Map)) return value;
        Map map = (Map) value;
        for (JsonType child : getChildren()) {
            currentLevels.add(child.getName());
            Object childValue = map.get(child.getName());
            if(childValue != null){
                map.put(child.getName(),child.castValue(childValue,currentLevels));
            }
            currentLevels.remove(currentLevels.size() - 1);
        }
        return map;
    }


    @Override
    public String toString() {
        return "JsonType{type=" +getType()+","+
                "name='" + name + '\'' +
                '}';
    }
    @Override
    public JsonType newEntity() {
        return new ObjectJsonType();
    }
}
