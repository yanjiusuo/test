package com.jd.workflow.soap.common.util;


import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.xml.schema.*;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 引用类型帮助类
 */
public class RefJsonTypeHelper {
    public static List<String> needCopyAttrs = Arrays.asList("desc","required","value","name","hidden","mock");
    public static JsonType simplifyJsonType(JsonType jsonType) {
        return simplifyJsonType(jsonType,null, false);
    }

    public static JsonType simplifyJsonType(JsonType jsonType,JsonType parent, boolean parentIsRef) {
        List<JsonType> genericTypes = new ArrayList<>();
        if (jsonType.getGenericTypes() != null) {
            for (JsonType genericType : jsonType.getGenericTypes()) {
                genericTypes.add(simplifyJsonType(genericType,null, false));
            }
        }

       /* if (StringUtils.isNotBlank(jsonType.getTypeVariableName())
         &&  parent instanceof ObjectJsonType

        ) { // 这个地方处理的是
            RefObjectJsonType result = new RefObjectJsonType();
            result.setRefName(null);
            jsonType.cloneTo(result);
            result.setGenericTypes(genericTypes);
            return result;
        }*/

        if (jsonType instanceof ComplexJsonType) {
            JsonType clone = jsonType.newEntity();
            List<JsonType> children = new ArrayList<>();
            jsonType.cloneTo(clone);
            clone.setGenericTypes(genericTypes);
            for (JsonType child : ((ComplexJsonType) jsonType).getChildren()) {
                JsonType cloneChild = simplifyJsonType(child,jsonType, jsonType instanceof RefObjectJsonType);
                if(cloneChild != null){
                    children.add(cloneChild);
                }

            }
            ((ComplexJsonType)clone).setChildren(children);

            if(clone instanceof RefObjectJsonType) return clone;
            if(parentIsRef) {
                if(children.isEmpty()) return null;
                return clone;
            }else{
                return clone;
            }

        }
        if(parentIsRef) return null;
        return jsonType;

    }


    private static JsonType findByName(List<JsonType> children, String name) {
        if (children == null) return null;
        for (JsonType child : children) {
            if (name.equals(child.getName())) return child;
        }
        return null;
    }

    public static JsonType instanceJsonType(JsonType jsonType, Map<String, JsonType> refJsonTypes) {
        List<JsonType> genericTypes = new ArrayList<>();
        if (jsonType.getGenericTypes() != null) {
            for (JsonType genericType : jsonType.getGenericTypes()) {
                genericTypes.add(instanceJsonType(genericType, refJsonTypes));
            }
        }
        if (jsonType instanceof ComplexJsonType) {
            ComplexJsonType result = (ComplexJsonType) jsonType.clone();
            List<JsonType> actualChildren = new ArrayList<>();
            List<JsonType> refChildren = null;
            if (jsonType instanceof RefObjectJsonType) {
                final String refName = ((RefObjectJsonType) jsonType).getRefName();
                ComplexJsonType actualType = (ObjectJsonType) refJsonTypes.get(refName);
                if(actualType == null){
                    throw new BizException("无效的引用："+refName);
                }
                actualChildren = actualType.getChildren();
                refChildren = ((ComplexJsonType)jsonType).getChildren();
                result = new RefObjectJsonType();
                actualType.cloneTo(result);

                for (String needCopyAttr : needCopyAttrs) {
                    BeanTool.setProp(result,needCopyAttr,BeanTool.getProp(jsonType,needCopyAttr));
                }

            } else {
                actualChildren = ((ComplexJsonType) jsonType).getChildren();
                refChildren = new ArrayList<>();
            }
            List<JsonType> children = new ArrayList<>();

            for (JsonType child : actualChildren) {
                JsonType found = findByName(refChildren, child.getName());
                if (found == null) {
                    children.add(instanceJsonType(child, refJsonTypes));
                }else{
                    children.add(instanceJsonType(found, refJsonTypes));
                }

            }
            result.setChildren(children);
            result.setGenericTypes(genericTypes);
            return result;
        } else {
            jsonType.setGenericTypes(genericTypes);
            return jsonType;
        }
    }
}
