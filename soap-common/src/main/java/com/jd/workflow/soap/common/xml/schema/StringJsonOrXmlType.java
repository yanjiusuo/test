package com.jd.workflow.soap.common.xml.schema;

public enum StringJsonOrXmlType {
    string_json,string_xml;//,string_json_array,string_xml_array;

    public static StringJsonOrXmlType from(String name){
        for (StringJsonOrXmlType value : values()) {
            if(value.name().equalsIgnoreCase(name)){
                return value;
            }
        }
        return null;
    }
    /*public String getRawType(){
        if(isObject()) return "object";
        return "array";
    }*/
  /*  public boolean isObject(){
        return string_json_object.equals(this)
                || string_xml_object.equals(this);
    }
    public boolean isArray(){
        return string_json_array.equals(this)
                || string_xml_array.equals(this);
    }*/
}
