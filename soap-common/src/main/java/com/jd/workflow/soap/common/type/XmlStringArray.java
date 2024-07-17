package com.jd.workflow.soap.common.type;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.soap.common.type.serializer.XmlStringSerializer;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.util.ArrayList;
import java.util.List;
@JsonSerialize(using = XmlStringSerializer.class)
public class XmlStringArray<V>  extends ArrayList<V> implements IStringType {
    JsonType jsonType;
    public XmlStringArray(List list) {
        super(list);
    }
    public XmlStringArray() {
        super();
    }

    public JsonType getJsonType() {
        return jsonType;
    }

    public void setJsonType(JsonType jsonType) {
        this.jsonType = jsonType;
    }

    @Override
    public String toString() {
        return new XmlStringSerializer().xmlValue(this);
    }


    @Override
    public Object internalValue() {
        return new ArrayList(this);
    }
}
