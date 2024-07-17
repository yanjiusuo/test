package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.xml.SoapUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.expr.ExprNodeUtils;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;
@Data
public class WebServiceBaseStepMetadata extends FallbackStepMetadata {
    /**
     * 实体记录id
     */
    Long entityId;
    /**
     * 操作名称
     */
    String opName;



    /**
     soap action，有些场景下不指定会报错：https://blog.csdn.net/kthq/article/details/1823686
     */
    String soapAction;

    /**
     * 服务端地址链接
     */
    List<String> endpointUrl;
    /**
     * url请求路径，endpointUrl+url共同拼接成了请求地址
     */
    String url;

    TaskDefinition taskDef;

    CustomMvelExpression successCondition;
    /**
     * 输入数据
     */
    WebServiceStepMetadata.Metadata input;
    /**
     * 输出数据
     */
    WebServiceStepMetadata.Metadata output;
    public void init(){
        super.init();
        if(input == null){
            throw new StepParseException("webservice.err_input_is_required").param("id",id);
        }
        if(output == null){
            throw new StepParseException("webservice.err_output_required").param("id",id);
        }
        if(endpointUrl == null || endpointUrl.isEmpty()){
            throw new StepParseException("step.err_endpoint_url_is_required").param("id",id);
        }

        MvelUtils.compileJsonTypeValue(input.getHeader(),id,"input.header");
        MvelUtils.compileJsonTypeValue(input.getBody(),id,"input.body");
        MvelUtils.compileJsonTypeValue(input.getSchemaType(),id,"input.schemaType");
        MvelUtils.compileJsonTypeValue(output.getHeader(),id,"output.header");
        MvelUtils.compileJsonTypeValue(output.getBody(),id,"output.body");
        MvelUtils.compileJsonTypeValue(output.getSchemaType(),id,"output.schemaType");
        MvelUtils.compile(id,"successCondition",successCondition);


    }

    @Override
    public void buildTreeNode(ExprTreeNode parent) {
        if(input != null){
            ExprTreeNode inputNode = new ExprTreeNode("输入","object",parent.getExpr()+".input");
            ExprTreeNode bodyNode = new ExprTreeNode("body","object",inputNode.getExpr()+".body");
            inputNode.addChild(bodyNode);
            parent.addChild(inputNode);

        }
        if(output != null){
            ExprTreeNode inputNode = new ExprTreeNode("输出","object",parent.getExpr()+".output");
            parent.addChild(inputNode);
            if(isWrappedMessage()){
                JsonType bodyType = (ObjectJsonType) JsonTypeUtils.get(output.getSchemaType(),"Body");
                if(bodyType == null || !(bodyType instanceof ObjectJsonType)
                        || ((ObjectJsonType)bodyType).getChildren().isEmpty()
                ){
                    return;
                }
                ObjectJsonType objectJsonType = (ObjectJsonType) bodyType;
                ExprNodeUtils.buildBodyExprNode(inputNode,objectJsonType.getChildren().get(0));
            }else{
                JsonType bodyType = (ObjectJsonType) JsonTypeUtils.get(output.getSchemaType(),"Body");

                ExprNodeUtils.buildBodyExprNode(inputNode,bodyType);
            }

        }
    }

    @Override
    public String buildPseudoCode() {
        String template = "callWebservice({\"stepId\":\"%s\"});";
        return String.format(template,getId());
    }

    boolean isWrappedMessage(){
        return SoapUtils.isWrappedMessage(getInput().getSchemaType(),getOpName());
    }

    public void mergeFrom(WebServiceStorageMetadata metadata){
        metadata.init();
        WebServiceStorageMetadata.StorageData input = metadata.getInput();
        this.input.mergeFrom(metadata.getInput());
        this.output.mergeFrom(metadata.getOutput());
    }




    @Override
    public CustomMvelExpression getSuccessCondition() {
        return this.successCondition;
    }

    @Data
    public static class Metadata{
        /**
         * webservice input、output生成的schemaType报文，SoapOperationToJsonTransformer生成
         */
        JsonType schemaType;

        /**
         * 前端维护的，是冗余存储,webservice转http使用
         */
        List<JsonType> header;
        /**
         * 前端维护的，是冗余存储,webservice转http使用
         */
        List<JsonType> body;



        public void mergeFrom(WebServiceStorageMetadata.StorageData data){

            JsonType envelop = data.getSchemaType();
            ObjectJsonType header = (ObjectJsonType) JsonTypeUtils.get(envelop, "Header");
            ObjectJsonType body = (ObjectJsonType) JsonTypeUtils.get(envelop, "Body");
            JsonTypeUtils.mergeValue(header.getChildren(),this.header);
            JsonTypeUtils.mergeValue(body.getChildren(),this.body);

            this.schemaType = envelop;
        }

    }


}