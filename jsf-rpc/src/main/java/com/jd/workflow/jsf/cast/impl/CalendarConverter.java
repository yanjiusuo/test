package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public class CalendarConverter implements JsfParamConverter {
    @Override
    public Object write(JsonType currentJsonType, Object value) {
        if(StringHelper.isNumber((String)value)){
            long time = Variant.valueOf(value).toLong();
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(time);
            return calendar;
        }
        final Timestamp timestamp = Variant.valueOf(value).toTimestamp();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getTime());
        return calendar;
    }

    @Override
    public Object getDemoValue(Class type) {

        return StringHelper.formatDate(new Date(),"yyyy-MM-dd HH:mm:ss");
    }
}
