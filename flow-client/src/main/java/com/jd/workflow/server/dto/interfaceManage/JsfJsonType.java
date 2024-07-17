package com.jd.workflow.server.dto.interfaceManage;


import java.util.ArrayList;
import java.util.List;


public class JsfJsonType {
    /**
     * 字段名称
     */
    String name;
    /**
     * 字段类型
     * 复杂类型有array和object，array和object可以有children节点
     * 简单类型有： long、double、string、float、file、integer、boolean
     *
     * @return
     */
    String type;

    /**
     * 字段描述
     */
    String desc;

    /**
     * 是否隐藏
     */
    Boolean hidden;

    /**
     * 手必填
     */
    boolean required;

    /**
     * 默认值以及mock属性。前端录入的时候为mock值.以@开头的为mock规则
     * 转换的时候使用此属性。前端录入的时候为mock值.以@开头的为mock规则
     */
    Object value;
    /**
     * 泛型类型，java类型需要有泛型类型，
     */
    List<JsfJsonType> genericTypes;

    /**
     *  复杂类型有array和object，array和object可以有children节点
     */

    List<JsfJsonType> children = new ArrayList<>();
    /**
     * 实际类型名
     */
    String className;
    /**
     * 引用类型名称
     */
    String refName;



    public JsfJsonType(String type) {
        this.type = type;
    }

    public static JsfJsonType newEntity(String type){
        return new JsfJsonType(type);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Boolean getHidden() {
        return hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<JsfJsonType> getGenericTypes() {
        return genericTypes;
    }

    public void setGenericTypes(List<JsfJsonType> genericTypes) {
        this.genericTypes = genericTypes;
    }

    public List<JsfJsonType> getChildren() {
        return children;
    }

    public void setChildren(List<JsfJsonType> children) {
        this.children = children;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getRefName() {
        return refName;
    }

    public void setRefName(String refName) {
        this.refName = refName;
    }
}
