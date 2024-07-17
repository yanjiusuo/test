package com.jd.workflow.soap.common.type;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.soap.common.type.serializer.XmlStringSerializer;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@JsonSerialize(using = XmlStringSerializer.class)
public class XmlStringObject<K,V>  extends HashMap<K,V> implements IStringType {
    JsonType jsonType;
    public XmlStringObject() {
      super();
    }
    public XmlStringObject(Map map) {
        super(map);
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
    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();

        XmlStringObject obj = new XmlStringObject();
        obj.put("sid",123);
        map.put("a",obj);
        String s = JsonUtils.toJSONString(map);
        System.out.println("result+is::"+s);


    }



    @Override
    public Object internalValue() {
        return new LinkedHashMap(this);
    }
}
