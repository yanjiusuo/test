package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.soap.common.enums.ExprType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.expr.ExprNodeUtils;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class HttpStepMetadata extends FallbackStepMetadata {

    Input input;
    Output output;

    /**
     * 调用环境
     */
    protected String env;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 方法id
     */
    private Long entityId;


    /**
     * 接口Id
     */
    private Long interfaceID;
    /**
     * 要访问的实际地址
     */
    protected List<String> endpointUrl;
    CustomMvelExpression successCondition;

    TaskDefinition taskDef;

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

    @Override
    public void init() {
        super.init();

        MvelUtils.compile(id,"script",successCondition);
        if(input != null){
            if(input.getReqType() == null){
               input.reqType = ReqType.json.name();
            }
            if(input.getMethod() == null){
                throw new StepParseException("httpstep.err_method_is_required").id(id);
            }
            if(endpointUrl == null || endpointUrl.isEmpty()){
                throw new StepParseException("step.err_endpoint_url_is_required").param("id",id);
            }
            MvelUtils.compile(id,"preProcess",input.preProcess);
            MvelUtils.compile(id,"script",input.script);
            MvelUtils.compileSimpleJsonTypeValue(input.getParams(),id,"params");
            MvelUtils.compileSimpleJsonTypeValue(input.getHeaders(),id,"headers");
            MvelUtils.compileSimpleJsonTypeValue(input.getPath(),id,"path");
            MvelUtils.compileJsonTypeValue(input.getBody(),id,"body");
        }
        if(output != null){
            MvelUtils.compile(id,"script",input.script);
            MvelUtils.compileSimpleJsonTypeValue(output.getHeaders(),id,"headers");
            MvelUtils.compileJsonTypeValue(output.getBody(),id,"body");
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
    public String buildPseudoCode() {
        String template = "callHttp({\"stepId\":\"%s\"});";
        return String.format(template,getId());
    }

    @Data
    public static class Output{

        List<SimpleJsonType> headers;
        List<JsonType> body;
        CustomMvelExpression script;
    }
}
