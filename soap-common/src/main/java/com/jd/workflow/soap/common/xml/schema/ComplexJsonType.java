package com.jd.workflow.soap.common.xml.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ComplexJsonType extends JsonType{
    public abstract List<JsonType> getChildren();

    public abstract void setChildren(List<JsonType> children);
    public Map<String,Object> toJson(){
        Map<String,Object> map = super.toJson();
        map.put("type",getType());
        if(getChildren() != null){
            map.put("children",getChildren().stream().map(vs->vs.toJson()).collect(Collectors.toList()));
        }
        return map;
    }

    @Override
    public void cloneTo(JsonType jsonType) {

        List<JsonType> children = new ArrayList<>(getChildren());
        if(jsonType instanceof ComplexJsonType) {
            ((ComplexJsonType) jsonType).setChildren(children);
        }
        super.cloneTo(jsonType);
    }

}
