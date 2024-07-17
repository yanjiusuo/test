package com.jd.workflow.soap.common.type;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.soap.common.type.serializer.JsonStringSerializer;
import com.jd.workflow.soap.common.util.JsonUtils;

import java.util.ArrayList;
import java.util.List;
@JsonSerialize(using = JsonStringSerializer.class)
public class JsonStringArray <V>  extends ArrayList<V> implements IStringType {
    public JsonStringArray(List list) {
        super(list);
    }
    public JsonStringArray() {
        super();
    }
    @Override
    public String toString() {
        return JsonUtils.toJSONString(new ArrayList<>(this));
    }



    @Override
    public Object internalValue() {
        return new ArrayList(this);
    }
}
