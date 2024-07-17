package com.jd.workflow.soap.example.jsf.entity;

import lombok.Data;

@Data
public class SimpleTypeClass {
    int intVar;
    short shortVar;
    long longVar;
    float floatVar;
    double doubleVar;
    char charVar;
    //@XmlElement(required = true)
    boolean booleanVar;
    byte[] bytesVar;
    int[] intsVar;
}
