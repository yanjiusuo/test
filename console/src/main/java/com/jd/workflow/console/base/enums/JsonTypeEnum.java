package com.jd.workflow.console.base.enums;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JsonTypeEnum {
    OBJECT("object"),
    ARRAY("array"),
    SIMPLE_LONG("long"),
    SIMPLE_DOUBLE("double"),
    SIMPLE_STRING("string"),
    SIMPLE_FLOAT("float"),
    SIMPLE_INTEGER("integer"),
    SIMPLE_BOOLEAN("boolean"),
        ;
    String name;

}
