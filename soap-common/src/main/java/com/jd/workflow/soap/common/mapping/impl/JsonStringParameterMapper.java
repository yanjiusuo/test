package com.jd.workflow.soap.common.mapping.impl;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.soap.common.type.JsonStringArray;
import com.jd.workflow.soap.common.type.JsonStringObject;
import com.jd.workflow.soap.common.mapping.CommonParamMappingUtils;
import com.jd.workflow.soap.common.mapping.ICustomParameterMapper;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonStringUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.JsonTypeSerializer;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 确保json序列化的时候值能够是 字符串json
 */
@JsonSerialize(using = JsonStringParameterMapper.class)
public class JsonStringParameterMapper extends JsonSerializer<JsonStringParameterMapper> implements ICustomParameterMapper {
    private Object mapperValue;
    public JsonStringParameterMapper(){}
    public JsonStringParameterMapper(Object mapperValue){
        this.mapperValue = mapperValue;
    }


    @Override
    public Object evaluate(CommonParamMappingUtils utils, CommonParamMappingUtils.EvalContext context) {
        Object value = utils.replace(mapperValue, context);

        return JsonStringUtils.castJsonStringValue(value);
    }

    @Override
    public void serialize(JsonStringParameterMapper value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(JsonUtils.toJSONString(value.mapperValue));
    }
}
