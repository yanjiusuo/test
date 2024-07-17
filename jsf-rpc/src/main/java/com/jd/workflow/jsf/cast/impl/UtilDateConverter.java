package com.jd.workflow.jsf.cast.impl;

import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.sql.Date;

public class UtilDateConverter implements JsfParamConverter {
    @Override
    public Object write(JsonType currentJsonType, Object value) {
        return Variant.valueOf(value).toDate();
    }

    @Override
    public Object getDemoValue(Class type) {
        return StringHelper.formatDate(new Date(System.currentTimeMillis()),"yyyy-MM-dd HH:mm:ss");
    }


}
