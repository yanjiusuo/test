package com.jd.workflow.flow.core.definition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.flow.core.camel.RouteStepBuilder;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.parser.context.IFlowParserContext;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonSerialize(using = StepDefinition.StepSerializer.class)
public interface StepDefinition extends RouteStepBuilder {
    String getId();
    void setId(String id);
    public void parseMetadata(Map<String,Object> config, IFlowParserContext context, boolean init);
    default public void parseMetadata(Map<String,Object> config,IFlowParserContext context){
         parseMetadata(config,context,true);
    }
    public StepMetadata getMetadata();
    public void setMetadata(StepMetadata metadata);
    public Map<String,Object> getConfig();
    public String getType();

    /**
     * children可能会出现分支，不同分支的步骤id可以重复
     * @return
     */
    default Map<Integer,List<StepDefinition>> getChildren(){
        return Collections.emptyMap();
    }

     public static class StepSerializer extends JsonSerializer<StepDefinition> {

        @Override
        public void serialize(StepDefinition value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeObject(value.getMetadata());

        }
    }

}
