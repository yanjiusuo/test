package com.jd.workflow.soap.common.xml.schema;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class JsonTypeSerializer extends JsonSerializer<JsonType> {

    @Override
    public void serializeWithType(JsonType value, JsonGenerator gen, SerializerProvider serializers, TypeSerializer typeSer) throws IOException {
        //super.serializeWithType(value, gen, serializers, typeSer);
        gen.getCodec().writeValue(gen, value.toJson());
    }

    @Override
    public void serialize(JsonType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.getCodec().writeValue(gen, value.toJson());
    }
}
