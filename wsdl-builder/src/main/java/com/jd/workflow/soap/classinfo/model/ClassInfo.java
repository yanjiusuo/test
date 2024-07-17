package com.jd.workflow.soap.classinfo.model;

import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Objects;

@Data
public class ClassInfo {
    String name;
    String packageName;
    List<FieldInfo> fields;

    public String fullClassName(){
        if(StringUtils.isEmpty(packageName)) return name;
        return packageName+"."+name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClassInfo classInfo = (ClassInfo) o;
        return Objects.equals(name, classInfo.name) &&
                Objects.equals(packageName, classInfo.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, packageName);
    }
}
