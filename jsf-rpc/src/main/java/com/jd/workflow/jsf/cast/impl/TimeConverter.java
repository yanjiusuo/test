package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.sql.Time;
import java.util.Calendar;

public class TimeConverter implements JsfParamConverter {
    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(ObjectHelper.isEmpty(value)) return null;
        if(StringHelper.isNumber((String)value)) {
            long time = Variant.valueOf(value).toLong();
            return new Time(time);
        }
        return Time.valueOf((String)value);

    }
}
