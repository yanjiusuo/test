package com.jd.workflow.soap.wsdl.param;

import com.jd.workflow.soap.common.xml.schema.SimpleParamType;

import java.util.List;
import java.util.Map;

public enum ParamType {
    ARRAY,OBJECT,LONG,INTEGER,DOUBLE,FLOAT,BOOLEAN,STRING;
    ParamType(){}
    public boolean hasChildren(){
        return ARRAY.equals(this) || OBJECT.equals(this);
    }
    public Class getType(){
        if(ARRAY.equals(this)) return List.class;
        else if(OBJECT.equals(this)) return Map.class;
        //else if(NUMBER.equals(this)) return Number.class;
        else if(LONG.equals(this)) return Long.class;
        else if(INTEGER.equals(this)) return Integer.class;
        else if(DOUBLE.equals(this)) return Double.class;
        else if(FLOAT.equals(this)) return Float.class;
        else if(BOOLEAN.equals(this)) return Boolean.class;
        else if(STRING.equals(this)) return String.class;
        return null;
    }
    public boolean isObject(){
        return OBJECT.equals(this);
    }
    public boolean isArray(){
        return ARRAY.equals(this);
    }
    public static ParamType from(String type){
        for (ParamType value : values()) {
            if(type.equalsIgnoreCase(value.name())){
                return value;
            }
        }
        return null;
    }
}
