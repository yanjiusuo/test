package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.utils.JsonValidateUtils;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.expr.ExprNodeUtils;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.Data;

import java.util.List;

@Data
public class CollectStepMetadata extends StepMetadata {
    protected OutputCollectMetadata output;

    public void init(){
        if(output == null){
            //throw new StepParseException("failOutput.err_output_is_required");
            return;
        }

        output.compile(id,"output");

    }

    @Override
    public void buildTreeNode(ExprTreeNode parent) {
        if(output == null) return;
        ExprTreeNode outputNode = new ExprTreeNode("输出","object",parent.getExpr()+".output");
        parent.addChild(outputNode);
        ExprNodeUtils.buildExprNode(outputNode,output.getHeaders(),"headers");
        ExprNodeUtils.buildBodyExprNode(outputNode,output.getBody());
    }

    @Override
    public String buildPseudoCode() {
        String template = "collect({\"stepId\":\"%s\"});";
        return String.format(template,getId());
    }
}
