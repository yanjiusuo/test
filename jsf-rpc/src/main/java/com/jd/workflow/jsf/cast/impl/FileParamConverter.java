package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

public class FileParamConverter implements JsfParamConverter<URL> {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(value instanceof Map) {
            final String path = (String) ((Map<?, ?>) value).get("path");
            if(StringUtils.isEmpty(path)) return null;
            return new File(path);
        }
        if(!(value instanceof String)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","string")
                    .param("actual",value);
        }
        return new File((String)value);
    }

    @Override
    public Object getDemoValue(Class type) {
        return "/export/logs/default.log";
    }
}
