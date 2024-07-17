package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class URIParamConverter implements JsfParamConverter<URI> {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(!(value instanceof String)){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","string")
                    .param("actual",value);
        }
        try {
            return new URI((String)value);
        } catch ( URISyntaxException e) {
            throw new TypeConvertException("typeconvert.err_invalid_url_pattern")
                    .param("prop",currentJsonType.getName())
                    .param("value","uri")
                    .param("actual",value);
        }
    }

    @Override
    public Object getDemoValue(Class type) {
        return "http://xx.com/a/123";
    }
}
