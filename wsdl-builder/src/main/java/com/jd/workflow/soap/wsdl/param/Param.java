package com.jd.workflow.soap.wsdl.param;

import com.jd.workflow.soap.classinfo.model.ClassInfo;
import com.jd.workflow.soap.classinfo.model.FieldInfo;
import lombok.Data;
import org.apache.cxf.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Data
public class Param {
    String name;
    String className;
    boolean required = false;
    ParamType paramType;
    List<Param> children = new ArrayList<>();

    public Param() {
    }


    public Param(String name, ParamType paramType) {
        this.name = name;
        this.paramType = paramType;
    }

    public boolean hasChildren() {
        return paramType.hasChildren();
    }

    public boolean isObject() {
        return paramType.isObject();
    }
    public boolean isArray() {
        return paramType.isArray();
    }

    public String fullTypeName(String pkgName) {
        if (!StringUtils.isEmpty(className)) {
            return pkgName + "." + className;
        }
        if (isObject()) {
            return pkgName + "." + StringUtils.capitalize(name);
        } else if(isArray()){
            Param child = children.get(0);
            String childTypeName = child.fullTypeName(pkgName);
            return childTypeName+"[]";//"ArrayList<"+  + ">";
        }else {
            return paramType.getType().getName();
        }

    }
    public String simpleClassName(String name) {
        if(StringUtils.isEmpty(name)){
            return name;
        }
        if (!StringUtils.isEmpty(className)) {
            return className;
        }

        return StringUtils.capitalize(name);

    }
    public String simpleClassName() {
        return simpleClassName(null);
    }
    public ClassInfo toClass(String packageName){
        return toClass(packageName,null);
    }
    public ClassInfo toClass(String packageName,String className) {
        if (!isObject()) {
            return null;
        }
        ClassInfo classInfo = new ClassInfo();
        classInfo.setPackageName(packageName);
        classInfo.setName(simpleClassName(className));
        classInfo.setFields(new ArrayList<>());
        if (children != null) {
            for (Param child : children) {
                classInfo.getFields().add(new FieldInfo(child.getName(), child.fullTypeName(packageName)));
            }
        }
        return classInfo;
    }
}
