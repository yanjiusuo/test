package com.jd.workflow.soap.common.xml.schema;

import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.ObjectHelper;
import org.apache.commons.lang.StringUtils;

import java.io.File;

public enum SimpleParamType {
    LONG,DOUBLE,STRING,FLOAT,FILE,INTEGER,BOOLEAN,_TEXT,_ATTR;
    SimpleParamType(){}
    public String typeName(){
        if(_TEXT.equals(this)) return "#text";
        if(_ATTR.equals(this)) return "#attr";
        return name().toLowerCase();
    }
    public Class getType(){
        if(LONG.equals(this)) return Long.class;
        else if(DOUBLE.equals(this)) return Double.class;
        else if(STRING.equals(this)) return String.class;
        else if(FLOAT.equals(this)) return Float.class;
        else if(INTEGER.equals(this)) return Integer.class;
        else if(BOOLEAN.equals(this)) return Boolean.class;
        else if(_TEXT.equals(this)) return String.class;
        else if(_ATTR.equals(this)) return String.class;
        else if(FILE.equals(this)) return File.class;
        return null;
    }
    public Object castValue(Object value){
        if(ObjectHelper.isEmpty(value)) return null;
        if(LONG.equals(this)) return Variant.valueOf(value).toLong();
        else if(DOUBLE.equals(this)) return Variant.valueOf(value).toDouble();
        else if(FLOAT.equals(this)) return Variant.valueOf(value).toFloat();
        else if(INTEGER.equals(this)) return Variant.valueOf(value).toInt();
        else if(BOOLEAN.equals(this)) return Variant.valueOf(value).toBool();


        return  value;
    }
    public static SimpleParamType from(String str){
        for (SimpleParamType value : values()) {
            if(value.getType().getSimpleName().equalsIgnoreCase(str)){
                return value;
            }
        }
        return null;
    }
    public  String mock(SimpleJsonType jsonType){
        String prefix = "@string";
        String suffix = "";
        if(jsonType.getConstraint() != null){
            Number min = jsonType.getConstraint().getMin();
            Number max = jsonType.getConstraint().getMax();
            if(min != null && max != null) {
                suffix = "(" + min + "," + max + ")";
            }else if(min != null) {
                suffix = "(" + min + ")";
            }
        }
        if(LONG.equals(this)) {
            prefix = "@integer"+suffix;
        }
        else if(DOUBLE.equals(this)) {
            prefix = "@float"+suffix;
        }else if(STRING.equals(this) || _TEXT.equals(this) ||_ATTR.equals(this) ) {
            prefix = "@string"+suffix;
        }
        else if(FLOAT.equals(this)) {
            prefix = "@float"+suffix;
        }
        else if(INTEGER.equals(this)) {
            prefix = "@integer"+suffix;
        }
        else if(BOOLEAN.equals(this)) {
            prefix = "@boolean";
        }
        else if(FILE.equals(this)) {
            prefix = null;
        }
        return prefix;
    }
}
