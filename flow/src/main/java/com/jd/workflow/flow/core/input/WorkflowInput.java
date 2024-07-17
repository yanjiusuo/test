package com.jd.workflow.flow.core.input;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.workflow.flow.core.attr.AttributeSupport;
import com.jd.workflow.flow.core.step.StepContext;
import lombok.Data;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
@Data
public class WorkflowInput extends BaseInput {
    Map<String,Object> params = new LinkedHashMap<>();
    String method;
    private Object body;
 

    private Map<String,Object> headers = new LinkedCaseInsensitiveMap<>();
    String url;

    public void addHeader(String key,Object value){
        headers.put(key,value);
    }
    public void addAllHeaders(Map<String,Object> map){
        headers.putAll(map);
    }
    public Object getHeader(String name){
        return headers.get(name);
    }
    public void addParam(String key,Object value){
        params.put(key,value);
    }




    public Map<String,Object> toInputMap(){
        Map<String,Object> map = new HashMap<>();
        map.put("params",params);
        map.put("method",method);
        map.put("body",body);
        map.put("headers",headers);
        return map;
    }

}
