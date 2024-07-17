package com.jd.workflow.soap.common.xml.schema;

import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.type.JsonStringArray;
import com.jd.workflow.soap.common.type.JsonStringObject;
import com.jd.workflow.soap.common.type.XmlStringObject;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonStringUtils {
    public static Object castJsonStringValue(Object value){
        if(value == null) return "";
        if(value instanceof String ){
            String str  = ((String) value).trim();
            if(str.startsWith("{")){
                 value = JsonUtils.parse(str, Map.class);
            }else if(str.startsWith("[")){
                value = JsonUtils.parse(str,List.class);
            }
        }
        if(value instanceof Map && !(value instanceof JsonStringObject)){
            return new JsonStringObject<>((Map)value);
        }else if(value instanceof List && !(value instanceof JsonStringArray)){
            return new JsonStringArray<>((List)value);
        }
        return value;
    }
    public static Object castXmlStringValue(Object value,JsonType stringXmlChild){
        if(value == null) return "";
        // 已经转换过了
        if(value instanceof XmlStringObject) return value;
        if(value instanceof String){
            String str = ((String)value).trim();
            if(str.startsWith("<")){
                value = JsonTypeUtils.parseXmlByJsonType(stringXmlChild,(String)value);
                if(!(value instanceof Map)){
                    throw new StdException("map.err_invalid_string_xml_value").param("value",value);
                }
                Map map = (Map) value;
                value = map.get(stringXmlChild.getName());
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put(stringXmlChild.getName(),value);
        XmlStringObject<Object, Object> obj = new XmlStringObject<>(map);
        obj.setJsonType(stringXmlChild);
        return obj;

    }
}
