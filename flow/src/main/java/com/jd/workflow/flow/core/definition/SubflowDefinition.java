package com.jd.workflow.flow.core.definition;

import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.core.metadata.impl.SubflowStepMetadata;
import com.jd.workflow.flow.core.processor.StepProcessorRegistry;
import com.jd.workflow.flow.parser.context.IFlowParserContext;
import com.jd.workflow.flow.utils.ParserUtils;
import com.jd.workflow.flow.utils.TypeConverterUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;

import java.util.List;
import java.util.Map;

public class SubflowDefinition extends BaseStepDefinition{




    @Override
    public void parseMetadata(Map<String, Object> config, IFlowParserContext context, boolean init) {
        super.parseMetadata(config,context);

        SubflowStepMetadata stepMetadata = TypeConverterUtils.cast(config, SubflowStepMetadata.class,(String)config.get("id"));
        stepMetadata.setAnyAttrs(JsonUtils.getBeanAnyAttrs(stepMetadata,config));

        ParserUtils.notEmpty(stepMetadata.getEntityId(),"子流程entityId不可为空","subflow",stepMetadata.getId());
        context.pushFlowId(stepMetadata.getEntityId());
        WorkflowDefinition flowDefinition = context.resolveSubflow(stepMetadata.getEntityId(),stepMetadata);
        stepMetadata.setDefinition(flowDefinition);
        context.removeFlowId(stepMetadata.getEntityId());

        if(init){
            stepMetadata.init();
        }


        this.metadata = stepMetadata;
    }

    @Override
    public StepMetadata getMetadata() {
        return this.metadata;
    }

    @Override
    public void build(XNode parent,XNode root,String idPrefix) {
        SubflowStepMetadata stepMetadata = (SubflowStepMetadata) this.metadata;
        XNode node = RouteBuilder.makeBeanNode(idPrefix,id,config);
        parent.appendChild(node);
        XNode childRoute = RouteBuilder.buildRoute(stepMetadata.getDefinition(), root, id);
        //root.appendChild(childRoute);
    }
}
