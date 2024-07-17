package com.jd.workflow.soap.common.type.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jd.workflow.soap.common.type.JsonStringObject;
import com.jd.workflow.soap.common.util.JsonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonStringSerializer  extends JsonSerializer<Object> {

    @Override
    public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if(value == null) gen.writeString("");
        if(value instanceof  Map){
            Map map = new HashMap((Map)value);
            gen.writeString(JsonUtils.toJSONString(map));
            //gen.writeObject(map);
        }else if(value instanceof List){
            List list = (List) value;
            List newList = new ArrayList(list);
            gen.writeString(JsonUtils.toJSONString(newList));
            //gen.writeObject(newList);
        }else{
            throw new UnsupportedOperationException();
        }

    }

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();
        map.put("a",1);
        map.put("b",2);
        JsonStringObject jsonStringObject = new JsonStringObject(map);
        Map<String,Object> childMap = new HashMap<>();
        childMap.put("child",jsonStringObject);
        final String str1 = JsonUtils.toJSONString(jsonStringObject);
        final String str2 = JsonUtils.toJSONString(childMap);
        System.out.println(str1);
        System.out.println(str2);
    }

}
