package com.jd.workflow.jsf.metadata;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.jsf.enums.JsfRegistrySite;
import com.jd.workflow.soap.common.method.ColorGatewayParamDto;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.expr.ExprNodeUtils;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.Data;
import org.apache.camel.language.mvel.MvelExpression;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 {
   type:"string",
   value:"${workflow.input.params.id}"
 }
 {
  id:"${xxx}",
   name:"${xxx}"
 }
 */
@Data
public class JsfStepMetadata extends FallbackStepMetadata {
    /**
     * 入参
     */
    List<? extends JsonType> input;
    CustomMvelExpression preProcess;
    CustomMvelExpression script;
    /**
     * 出参
     */
    JsonType output;
    String interfaceName;
    String methodName;
    String alias;

    CustomMvelExpression successCondition;

    TaskDefinition taskDef;
    /**
     * jsfRegistrySite
     */
    String site;
    String env;
    String[] exceptions;
    List<? extends JsonType> attachments;
    String protocol;

    /**
     * 输出
     */
    ColorGatewayParamDto colorInput;


    /**
     * 输出
     */
    ColorGatewayParamDto colorOutput;
    /**
     * 测试链接，直连的时候使用
     */
    String url;
    public void initEmptyValue(){
        if(input == null){
            input = Collections.emptyList();
        }
        if(attachments == null){
            attachments = Collections.emptyList();
        }
    }
    @Override
    public void init(){
        super.init();
        MvelUtils.compile(id,"successCondition",successCondition);
        if(StringUtils.isEmpty(interfaceName)){
            throw new StepParseException("jsf.err_miss_required_param").param("value","interfaceName");
        }
        if(StringUtils.isEmpty(methodName)){
            throw new StepParseException("jsf.err_miss_required_param").param("value","methodName");
        }
        if(StringUtils.isEmpty(site)){
            site = "zh";
        }
        if(!JsfRegistrySite.zh.name().equals(site)){
            throw new StepParseException("jsf.err_only_zh_site_is_support");
        }
        if(StringUtils.isEmpty(alias)){
            throw new StepParseException("jsf.err_miss_required_param").param("value","alias");
        }
        if(StringUtils.isEmpty(env)){
            throw new StepParseException("jsf.err_miss_required_param").param("value","env");
        }
        if(input != null){
            MvelUtils.compileJsonTypeValue(input,id,"input");
        }
        MvelUtils.compile(id,"preProcess",preProcess);
        MvelUtils.compile(id,"script",script);
        if(output != null){

            MvelUtils.compileJsonTypeValue(output,id,"output");
        }
    }

    @Override
    public TaskDefinition getTaskDef() {
        return taskDef;
    }

    @Override
    public CustomMvelExpression getSuccessCondition() {
        return successCondition;
    }

    @Override
    public void buildTreeNode(ExprTreeNode parent) {

            ExprTreeNode inputNode = new ExprTreeNode("输入","object",parent.getExpr()+".input");
            parent.addChild(inputNode);
            if(attachments != null && !attachments.isEmpty()){
                ExprNodeUtils.buildExprNode(inputNode,getAttachments(),"headers");
            }
            if(input !=null && !input.isEmpty()){
                ArrayJsonType arrayJsonType = new ArrayJsonType();
                arrayJsonType.getChildren().addAll(input);
                ExprNodeUtils.buildBodyExprNode(inputNode, arrayJsonType);
            }


        if(output != null ){
            ExprTreeNode outputNode = new ExprTreeNode("输出","object",parent.getExpr()+".output");
            parent.addChild(outputNode);
            ExprNodeUtils.buildBodyExprNode(outputNode,output);
        }
    }

}
