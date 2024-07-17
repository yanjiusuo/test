package com.jd.workflow.soap.common.type;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.soap.common.type.serializer.JsonStringSerializer;
import com.jd.workflow.soap.common.util.JsonUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


@JsonSerialize(using = JsonStringSerializer.class)
public class  JsonStringObject<K,V>  extends HashMap<K,V> implements IStringType {

    public JsonStringObject() {
      super();
    }
    public JsonStringObject(Map map) {
        super(map);
    }

    @Override
    public String toString() {
        return JsonUtils.toJSONString(new HashMap<>(this));
    }

    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();

        JsonStringObject obj = new JsonStringObject();
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
