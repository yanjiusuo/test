package com.jd.workflow.jsf.cast.impl;

import com.jd.jsf.gd.util.StringUtils;
import com.jd.workflow.jsf.cast.JsfParamConverter;
import com.jd.workflow.jsf.exception.TypeConvertException;
import com.jd.workflow.soap.common.util.MathHelper;
import com.jd.workflow.soap.common.util.StringHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.net.URL;
import java.util.Random;

public class BigNumberConverter implements JsfParamConverter {

    @Override
    public Object write(JsonType currentJsonType, Object value) {
        String strValue = value.toString();
        if(StringUtils.isEmpty(strValue)) return null;
        if(!(StringHelper.isNumber(strValue))){
            throw new TypeConvertException("typeconvert.err_type_not_match")
                    .param("prop",currentJsonType.getName())
                    .param("expected","long")
                    .param("actual",value);
        }
        return strValue;
    }

    @Override
    public Object getDemoValue(Class type) {
        double v = MathHelper.random().nextDouble(100.0);
        return v+"";
    }
}
