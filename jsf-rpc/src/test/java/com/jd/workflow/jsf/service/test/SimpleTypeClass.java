package com.jd.workflow.jsf.service.test;

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
