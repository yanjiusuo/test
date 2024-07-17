package com.jd.workflow.flow.core.definition;

import com.jd.workflow.flow.core.camel.RouteStepBuilder;
import com.jd.workflow.flow.core.constants.WorkflowConstants;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.core.metadata.impl.ChoiceStepMetadata;
import com.jd.workflow.flow.parser.WorkflowParser;
import com.jd.workflow.flow.parser.context.IFlowParserContext;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.XNode;

import java.util.*;

/*
  Object stepContext = exchange.getProperty("stepContext");
  Object workflow = stepContext.buildEnv().get("workflow");
  Object steps = stepContext.buildArgs().get("steps");

  {
    id:'xxx',
    type:'choice',
    children:[{
        when:'',// 执行条件
        children:[]// 子节点
    },{
        when:'',// 执行条件
        children:[]// 子节点
    },{
        // 其他
        children:[]// 子节点
    }]
  }
 */
public class ChoiceDefinition extends BaseStepDefinition implements RouteStepBuilder{



    ChoiceStepMetadata metadata;


    public Map<Integer,List<StepDefinition>> getChildren() {
        Map<Integer,List<StepDefinition>> branchChildren = new LinkedHashMap<>();
        int index = 1;
        for (ChoiceStepMetadata.ChoiceStep step : metadata.getChildren()) {
            branchChildren.put(index,step.getChildren());
            index++;
        }

        return branchChildren;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void parseMetadata(Map<String, Object> config, IFlowParserContext parserContext, boolean init) {
        super.parseMetadata(config,parserContext);
        ChoiceStepMetadata metadata = new ChoiceStepMetadata();


        List<Map<String,Object>> children = (List<Map<String, Object>>) config.get("children");
        List<ChoiceStepMetadata.ChoiceStep> steps = new ArrayList<>();
        for (Map<String, Object> child : children) {
            ChoiceStepMetadata.ChoiceStep step  = new ChoiceStepMetadata.ChoiceStep();
            step.setWhen(CustomMvelExpression.mvel((String) child.get("when"),false));
            step.setKey((String) child.get("key"));
            List<Map<String,Object>> whenChildren = (List<Map<String, Object>>) child.get("children");
            step.setChildren(WorkflowParser.parseSteps(whenChildren,parserContext));
            Map<String, Object> anyAttrs = JsonUtils.getBeanAnyAttrs(step, child);
            step.setAnyAttrs(anyAttrs);
            steps.add(step);
        }

        metadata.setType((String) config.get("type"));
        metadata.setId((String) config.get("id"));
        metadata.setKey((String) config.get("key"));
        metadata.setChildren(steps);
        metadata.setAnyAttrs(JsonUtils.getBeanAnyAttrs(metadata,config));
        if(init){
            metadata.init();
        }

        this.metadata = metadata;
    }

    @Override
    public StepMetadata getMetadata() {
        return this.metadata;
    }




    @Override
    public void build(XNode parent,XNode root,String idPrefix) {
        XNode choice = XNode.make("choice");
        String id = idPrefix+metadata.getId();
        choice.attr("id",id).attr("customId","true");
        for (int i = 0; i < metadata.getChildren().size(); i++) {
            ChoiceStepMetadata.ChoiceStep child = metadata.getChildren().get(i);
            XNode when = XNode.make("when");

            if(child.getWhen() !=null){ // when分支
                when.makeChild("mvel").attr("customId","true")
                        .attr("id",id+"_condition"+i).content(WorkflowConstants.MVEL_ENV_EXPR+child.getWhen().getExpressionString());
                when.attr("id","分支"+(i+1));
            }else{// 最后一个分支
                when = XNode.make("otherwise");
                when.attr("id","otherwise").attr("customId","true");

            }
           /* if(i==metadata.getChildren().size() - 1 ){

            }else{

            }*/

            for (StepDefinition step : child.getChildren()) {
                step.build(when,root,idPrefix);
            }
            choice.appendChild(when);
        }

       /* Map<String,Object> desc = new HashMap<>();
        desc.put("id",metadata.getId());
        desc.put("type",metadata.getType());

        choice.makeChild("description").content(JsonUtils.toJSONString(desc));*/
        parent.appendChild(choice);
    }



}
