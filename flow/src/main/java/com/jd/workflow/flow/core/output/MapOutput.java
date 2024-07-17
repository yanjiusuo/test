package com.jd.workflow.flow.core.output;

import java.util.HashMap;
import java.util.Map;


public class MapOutput  extends HashMap implements Output {

    public MapOutput(Map<String,Object> output){

        super(output);
    }

    @Override
    public Exception getException() {
        return null;
    }

    @Override
    public void setException(Exception exception) {

    }

    @Override
    public boolean isSuccess() {
        return (boolean) get("isSuccess");
    }

    @Override
    public void setSuccess(boolean success) {
        put("isSuccess",success);
    }

    @Override
    public Object getBody() {
        return get("body");
    }

    @Override
    public void setBody(Object body) {
        put("body",body);
    }

    @Override
    public void attr(String name, Object value) {
        Map map = (Map) computeIfAbsent("attrs", vs -> {
            return new HashMap<>();
        });
        map.put(name,value);
    }

    @Override
    public Object attr(String name) {
        Map map = (Map) computeIfAbsent("attrs", vs -> {
            return new HashMap<>();
        });
        return map.get(name);
    }
}
