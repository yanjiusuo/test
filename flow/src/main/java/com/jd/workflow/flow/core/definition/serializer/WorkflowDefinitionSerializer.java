package com.jd.workflow.flow.core.definition.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.soap.common.xml.schema.JsonType;

import java.io.IOException;

public class WorkflowDefinitionSerializer extends JsonSerializer<WorkflowDefinition> {


    @Override
    public void serialize(WorkflowDefinition value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.getCodec().writeValue(gen, value);
    }
}
