package com.jd.workflow.soap.common.xml.schema;


import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class MapJsonType extends ObjectJsonType {


    @Override
    public String getType() {
        return "map";
    }

    @Override
    public Class getTypeClass() {
        return Map.class;
    }


    public static boolean isMapType(ObjectJsonType jsonType){
        return StringUtils.isNotBlank(jsonType.getClassName())
                && jsonType.getClassName().contains("Map") && jsonType.getGenericTypes() != null
                && jsonType.getGenericTypes().size() == 2
                && ObjectHelper.isEmpty(( jsonType).getChildren());
    }
    @Override
    public Object toExprValue(ValueBuilderAcceptor acceptor) {
        Object value = this.getExprValue();
        if(value == null){

            Map current = new LinkedHashMap<>();
            Object mKey = null;
            Object mVal = null;
            if(!ObjectHelper.isEmpty(children)){
                for (JsonType child : children) {
                    if("key".equals(child.getName())){
                        mKey = child.toExprValue(acceptor);
                    }else if("value".equals(child.getName())){
                        mVal = child.toExprValue(acceptor);
                    }
                }
                if(mKey != null) {
                    current.put(mKey, mVal);
            }
            }
            value = current;
        }

        if(acceptor != null) return acceptor.afterSetValue(value,this);
        return value;
    }



    @Override
    public JsonType newEntity() {
        return new MapJsonType();
    }
}
