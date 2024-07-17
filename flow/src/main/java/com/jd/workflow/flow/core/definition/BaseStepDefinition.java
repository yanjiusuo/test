package com.jd.workflow.flow.core.definition;

import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.parser.context.IFlowParserContext;
import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;

public abstract class BaseStepDefinition implements StepDefinition{
    String id;
    protected Map<String,Object> config;

    protected StepMetadata metadata;
    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Map<String, Object> getConfig() {
        return config;
    }

    public String getType(){
        return (String) config.get("type");
    }

    @Override
    public void parseMetadata(Map<String, Object> config, IFlowParserContext context) {
        this.config = config;
    }

    @Override
    public void setMetadata(StepMetadata metadata) {
        this.metadata = metadata;
    }
}
