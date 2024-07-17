package com.jd.workflow.flow.core.definition;


import com.jd.workflow.flow.core.camel.CamelStepBean;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.parser.context.IFlowParserContext;
import com.jd.workflow.flow.utils.TypeConverterUtils;
import com.jd.workflow.soap.common.lang.Variant;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.xml.XNode;

import java.util.Map;

public class BeanStepDefinition extends   BaseStepDefinition{

    public BeanStepDefinition(){
    }



    @Override
    public void build(XNode parent,XNode root,String prefix) {
        XNode child = XNode.make("bean");
        child.attr("beanType", CamelStepBean.class.getName())
                .attr("customId","true")
                .attr("id",prefix+metadata.getId())
                .makeChild("description").markCDATA(true).content(JsonUtils.toJSONString(metadata));
        parent.appendChild(child);
    }

    @Override
    public void parseMetadata(Map<String, Object> metadata, IFlowParserContext parserContext, boolean init) {
        super.parseMetadata(metadata,parserContext);
        Class<StepMetadata> clazz = StepProcessorRegistry.getMetadataType((String) metadata.get("type"));

        StepMetadata stepMetadata = TypeConverterUtils.cast(metadata, clazz,(String)metadata.get("id"));
        stepMetadata.setAnyAttrs(JsonUtils.getBeanAnyAttrs(stepMetadata,metadata));
        if(init){
            stepMetadata.init();
        }


        this.metadata = stepMetadata;
    }

    @Override
    public StepMetadata getMetadata() {
        return this.metadata;
    }


}
