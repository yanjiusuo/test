package com.jd.workflow.soap.common.xml.schema.expr;

import lombok.Data;

import java.util.*;

/**
 * 表达式树节点
 */
@Data
public class ExprTreeNode {
    public  ExprTreeNode(){
        this.key = this.level+"";
    }
    public  ExprTreeNode(String label,String type,String expr){
        this();
        this.label = label;
        this.type = type;
        this.expr = expr;
    }
    int level = 0;
    String key;
    String type;
    String label;
    String expr;
    boolean disabled;
    List<ExprTreeNode> children;
    public void addChild(ExprTreeNode child){
        if(children == null){
            children = new LinkedList<>();
        }
        child.setLevel(level+1);

        child.setKey(UUID.randomUUID().toString());

        children.add(child);
    }

}
