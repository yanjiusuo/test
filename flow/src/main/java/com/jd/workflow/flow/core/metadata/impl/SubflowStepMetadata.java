package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
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
public class SubflowStepMetadata extends FallbackStepMetadata {
    TaskDefinition taskDef;
    /**
     * 输入
     */
    Input input;
    Output output;
    Output failOutput;
    /**
     * 子流程id
     */
    String entityId;
    WorkflowDefinition definition;

    @Override
    public void init() {
        super.init();
        if(input != null){
            if(input.getPreProcess() != null){
                MvelUtils.compile(id,"input.preProcess",input.getPreProcess());
            }
            if(input.getScript()!=null){
                MvelUtils.compile(id,"input.script",input.getScript());
            }else{
                MvelUtils.compileJsonTypeValue(input.getHeaders(),id,"input.headers");
                MvelUtils.compileJsonTypeValue(input.getBody(),id,"input.body");
                MvelUtils.compileJsonTypeValue(input.getParams(),id,"input.params");
            }
        }
    }
    @Override
    public void buildTreeNode(ExprTreeNode parent) {
        if(input != null){
            ExprTreeNode inputNode = new ExprTreeNode("输入","object",parent.getExpr()+".input");
            parent.addChild(inputNode);
            ExprNodeUtils.buildExprNode(inputNode,input.getHeaders(),"headers");
            ExprNodeUtils.buildExprNode(inputNode,input.getParams(),"params");
            ExprNodeUtils.buildBodyExprNode(inputNode,input.getBody());
        }
        if(output != null){
            ExprTreeNode inputNode = new ExprTreeNode("输出","object",parent.getExpr()+".output");
            parent.addChild(inputNode);
            ExprNodeUtils.buildExprNode(inputNode,output.getHeaders(),"headers");
            ExprNodeUtils.buildBodyExprNode(inputNode,output.getBody());
        }
    }
    @Override
    public CustomMvelExpression getSuccessCondition() {
        return CustomMvelExpression.mvel("output != null && output.isSuccess();");
    }

    @Data
    public static class Input{
        String url;
        String method;
        String reqType;
        List<JsonType> params;
        List<JsonType> headers;
        List<SimpleJsonType> path;
        List<JsonType> body;
        CustomMvelExpression script;
        CustomMvelExpression preProcess;

    }
    @Data
    public static class Output{

        List<SimpleJsonType> headers;
        List<JsonType> body;

    }
}
