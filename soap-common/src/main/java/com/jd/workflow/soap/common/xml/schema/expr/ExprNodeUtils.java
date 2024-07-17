package com.jd.workflow.soap.common.xml.schema.expr;

import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.util.Collections;
import java.util.List;

public class ExprNodeUtils {
    public static void buildBodyExprNode(ExprTreeNode parent, JsonType bodyJsonType){
        if(bodyJsonType == null) return;
        buildBodyExprNode(parent, Collections.singletonList(bodyJsonType));
    }
    public static void buildBodyExprNode(ExprTreeNode parent, List<JsonType> bodyJsonTypes){
        if(bodyJsonTypes == null || bodyJsonTypes.isEmpty()) return;
        JsonType jsonType = bodyJsonTypes.get(0);
        JsonType clone = jsonType.clone();
        clone.setName("body");
        clone.buildExprNode(parent);
    }
    public static void buildExprNode(ExprTreeNode parent, List<? extends JsonType> jsonTypes,String prefix){
        ExprTreeNode current = new ExprTreeNode(prefix,"object",parent.getExpr()+"."+prefix);

        if(jsonTypes == null || jsonTypes.isEmpty()) return;
        for (JsonType jsonType : jsonTypes) {
            jsonType.buildExprNode(current);
        }
        parent.addChild(current);
    }
}
