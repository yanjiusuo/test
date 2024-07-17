package com.jd.workflow.soap.common.type.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jd.workflow.soap.common.type.XmlStringArray;
import com.jd.workflow.soap.common.type.XmlStringObject;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlStringSerializer extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) gen.writeString("");

        gen.writeString(xmlValue(value));

    }
    public String xmlValue(Object value){
        if (value instanceof XmlStringObject) {
            XmlStringObject obj = (XmlStringObject) value;
            Object mapperValue = obj.get(obj.getJsonType().getName());
            List<XNode> nodes = obj.getJsonType().transformToXml(mapperValue);
            return XNode.toXml(nodes);
        } else if (value instanceof XmlStringArray) {
            XmlStringArray arr = (XmlStringArray) value;
            List<XNode> nodes = arr.getJsonType().transformToXml(value);
            String result = XNode.toXml(nodes);

            return result;
        }else {
            throw new UnsupportedOperationException();
        }

    }

}