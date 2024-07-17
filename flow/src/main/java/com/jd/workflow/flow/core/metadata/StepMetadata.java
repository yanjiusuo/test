package com.jd.workflow.flow.core.metadata;




import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;

import java.util.List;
import java.util.Map;

public abstract class StepMetadata {
    protected String id;
    protected String type;
    /**
     * 步骤key,全局唯一
     */
    protected String key;

    /**
     * 前端有些字段需要存储，使用此字段存储
     * @return
     */
    Map<String,Object> anyAttrs;


    public void init(){}
    /**
     * 转换给前端对象的数据
     * @return
     */
     public Object save(){
        return this;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    @JsonAnyGetter
    public Map<String, Object> getAnyAttrs() {
        return anyAttrs;
    }
    @JsonAnySetter
    public void setAnyAttrs(Map<String, Object> anyAttrs) {
        this.anyAttrs = anyAttrs;
    }

    /**
     * 构造步骤节点需要的节点树
     * @param parent
     */
    public  void buildTreeNode(ExprTreeNode parent){}

    public TaskDefinition getTaskDef() {
        return null;
    }
    /**
     * 构建伪代码,
     * parent为顶级节点，需要一级构造
     * @return
     */
    public  String buildPseudoCode(){return "";}
}
