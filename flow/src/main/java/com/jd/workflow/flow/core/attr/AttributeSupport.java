package com.jd.workflow.flow.core.attr;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AttributeSupport {
    /**
     * 某个步骤的属性可能会被并行修改
     */
    protected Map<String,Object> attrs = new ConcurrentHashMap<>();
    /**
     * 用来存放临时定义的变量
     */
    @JsonIgnore
    Map<String,Object> variables = new HashMap<>();

    public void attr(String name, Object value) {
        attrs.put(name,value);
    }

    public Object attr(String name) {
        return attrs.get(name);
    }

   public Map<String, Object> getAttrs() {
        return attrs;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public  Map<String,Object> attrsMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("attrs",this.attrs);
        return map;
    }
}
