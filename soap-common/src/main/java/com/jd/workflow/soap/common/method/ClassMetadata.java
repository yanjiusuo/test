package com.jd.workflow.soap.common.method;

import java.util.LinkedList;
import java.util.List;

public class ClassMetadata {
    String className;
    String desc;
    /* groupId:artifactId:version */
    String pomPath;
    List<MethodMetadata> methods = new LinkedList<>();
    boolean isColor;


    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPomPath() {
        return pomPath;
    }

    public void setPomPath(String pomPath) {
        this.pomPath = pomPath;
    }

    public List<MethodMetadata> getMethods() {
        return methods;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void addMethod(MethodMetadata methodMetadata){
        methods.add(methodMetadata);
    }

    public boolean isColor() {
        return isColor;
    }

    public void setColor(boolean color) {
        isColor = color;
    }
}
