package com.jd.workflow.flow.core.expr;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CustomMvelExpressionSerializer extends JsonSerializer<CustomMvelExpression> {

    @Override
    public void serialize(CustomMvelExpression value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeString(value.getExpressionString());
    }

}
