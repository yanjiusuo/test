package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.util.Date;

public class SqlDateConverter implements JsfParamConverter {
    @Override
    public Object write(JsonType currentJsonType, Object value) {
        Date date = Variant.valueOf(value).toDate();
        return new java.sql.Date(date.getTime());
    }

    @Override
    public Object getDemoValue(Class type) {
        return StringHelper.formatDate(new Date(),"yyyy-MM-dd HH:mm:ss");
    }
}
