package com.jd.workflow.flow.core.input;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MapInput extends HashMap<String,Object> implements Input {

    public MapInput(Map<String,Object> input){

        super(input);
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
