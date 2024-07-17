package com.jd.workflow.flow.core.definition;

import com.jd.workflow.flow.core.camel.RouteBuilder;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.core.metadata.impl.MultiStepMetadata;
import com.jd.workflow.flow.core.metadata.impl.OutputCollectMetadata;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.flow.parser.context.IFlowParserContext;
import com.jd.workflow.flow.utils.StepContextHelper;
import com.jd.workflow.flow.utils.TypeConverterUtils;
import com.jd.workflow.soap.common.util.BeanTool;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.util.*;

/**
 * 聚合步骤
 */
public class MulticastDefinition extends   BaseStepDefinition{
    String id;
    List<StepDefinition> children;
    Map<String,Object> output;
    PropertyUtilsBean utils = new PropertyUtilsBean();
    MultiStepMetadata multiStepMetadata;
    public String getId() {
        return this.id;
    }


    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void parseMetadata(Map<String, Object> config, IFlowParserContext parserContext, boolean init) {
        super.parseMetadata(config,parserContext);
        MultiStepMetadata metadata = new MultiStepMetadata();
        List<Map<String,Object>> childDefs = (List) config.get("children");
        children = new ArrayList<>();

        for (Map<String, Object> child : childDefs) {
            StepDefinition childDef = WorkflowParser.parseStep(child,parserContext);

            children.add(childDef);
        }
        metadata.setType((String) config.get("type"));
        metadata.setId((String) config.get("id"));
        metadata.setKey((String) config.get("key"));

        metadata.setChildren(children);
        metadata.setOutput(TypeConverterUtils.cast(config.get("output"), OutputCollectMetadata.class,(String)config.get("id") +".output" ));
        Map<String,Object> anyAttrs = JsonUtils.getBeanAnyAttrs(metadata,config);
        metadata.setAnyAttrs(anyAttrs);
        if(init){
            metadata.init();
        }
        this.multiStepMetadata = metadata;
    }

    @Override
    public StepMetadata getMetadata() {
        return this.multiStepMetadata;
    }



    public Map<Integer,List<StepDefinition>> getChildren() {
        Map<Integer,List<StepDefinition>> branchChildren = new LinkedHashMap<>();
        branchChildren.put(1,children);
        return branchChildren;
    }

    public void setChildren(List<StepDefinition> children) {
        this.children = children;
    }

    public void setOutput(Map<String, Object> output) {
        this.output = output;
    }

    @Override
    public void build(XNode parent,XNode root,String idPrefix) {
        XNode multiNode = XNode.make("multicast");
        multiNode
                .attr("parallelProcessing","true")
                .attr("stopOnException","true");
        for (StepDefinition child : children) {
            child.build(multiNode,root,idPrefix);
        }
        Map<String,Object> args = new HashMap<>();
        args.put("id",multiStepMetadata.getId());
        args.put("type","collect");
        args.put("output",multiStepMetadata.getOutput());
        XNode collectNode = RouteBuilder.makeBeanNode(idPrefix,multiStepMetadata.getId(),args);
        parent.appendChild(multiNode);
        parent.appendChild(collectNode);
    }

}
