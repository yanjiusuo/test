package com.jd.workflow.soap.common.mapping.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.mapping.CommonParamMappingUtils;
import com.jd.workflow.soap.common.mapping.ICustomParameterMapper;
import com.jd.workflow.soap.common.type.XmlStringObject;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.util.XmlUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonStringUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XmlStringParameterMapper  implements ICustomParameterMapper {
    Object mapperValue;
    JsonType jsonType;

    public XmlStringParameterMapper(Object mapperValue,JsonType jsonType) {
        this.mapperValue = mapperValue;
        this.jsonType = jsonType;
    }

    public void setJsonType(JsonType jsonType) {
        this.jsonType = jsonType;
    }

    @Override
    public Object evaluate(CommonParamMappingUtils utils, CommonParamMappingUtils.EvalContext context) {
        Object value = utils.replace(mapperValue, context);
        if(ObjectHelper.isEmpty(value)){
            return value;
        }
        return JsonStringUtils.castXmlStringValue(value,jsonType);

    }


}
