package com.jd.workflow.soap.common.xml.schema;


import com.jd.workflow.soap.common.exception.ToXmlTransformException;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.stream.Collectors;


public class RefObjectJsonType extends ObjectJsonType {
    String refName;
    //String refType;

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }

   /* public String getRefType() {
        return refType;
    }

    public void setRefType(String refType) {
        this.refType = refType;
    }*/

    @Override
    public String getType() {
        return "ref";
    }

    @Override
    public boolean isSimpleType() {
        return false;
    }



    @Override
    public Class getTypeClass() {
        return Map.class;
    }

    @Override
    public Map<String,Object> toJson(){
        Map<String,Object> map = super.toJson();
        map.put("refName",refName);
       // map.put("refType",refType);



        return map;
    }

    @Override
    public void cloneTo(JsonType jsonType) {

        List<JsonType> children = new ArrayList<>(this.getChildren());
        if(jsonType instanceof ArrayJsonType){
            ((ArrayJsonType)jsonType).setChildren(children);
        }else if(jsonType instanceof ObjectJsonType){
            ((ObjectJsonType)jsonType).setChildren(children);
            if(jsonType instanceof RefObjectJsonType){
                ((RefObjectJsonType)jsonType).setRefName(refName);
            }
        }
        super.cloneTo(jsonType);
    }




    @Override
    public String toString() {
        return "JsonType{type=" +getType()+","+
                "name='" + name + '\'' +
                '}';
    }
    @Override
    public JsonType newEntity() {
        return new RefObjectJsonType();
    }
}
