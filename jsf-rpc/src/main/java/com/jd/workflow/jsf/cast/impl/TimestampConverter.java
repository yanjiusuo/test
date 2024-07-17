package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.sql.Timestamp;

public class TimestampConverter implements JsfParamConverter<Timestamp> {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        Timestamp timestamp = Variant.valueOf(value).toTimestamp();
        return timestamp;
    }
}
