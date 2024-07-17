package com.jd.workflow.flow.core.definition.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.parser.WorkflowParser;
import java.io.IOException;
import java.util.Map;

public class WorkflowDefinitionDeserializer  extends JsonDeserializer<WorkflowDefinition> {

    public WorkflowDefinitionDeserializer() {

    }

    @Override
    public WorkflowDefinition deserialize(JsonParser jsonParser, DeserializationContext ctx)
            throws IOException, JsonProcessingException {
        Map map = jsonParser.readValueAs(Map.class);
        if (map == null) return null;
        WorkflowDefinition definition = WorkflowParser.parse(map);

        return definition;
    }
}