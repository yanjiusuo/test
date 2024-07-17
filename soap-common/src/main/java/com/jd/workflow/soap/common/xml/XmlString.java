package com.jd.workflow.soap.common.xml;

public class XmlString {
    private String value;
    public static XmlString from(String value){
        XmlString xml = new XmlString();
        xml.value = value;
        return xml;
    }
    public String getValue(){
        return value;
    }
}
