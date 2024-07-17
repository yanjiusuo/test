package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.lang.type.ObjectTypes;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.commons.lang.StringUtils;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class ByteBufferConverter implements JsfParamConverter {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        String className = currentJsonType.getClassName();
        if(value instanceof Map) {
            value = ((Map<?, ?>) value).get("array");
        };
        if(!(value instanceof String)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","string")
                    .param("actual",value.getClass().getName());
        }
         byte[] bytes = StringHelper.decodeBase64((String) value);
        //map.put("cap")
        return ByteBuffer.wrap(bytes);
    }
}
