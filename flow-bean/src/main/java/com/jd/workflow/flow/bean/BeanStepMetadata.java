package com.jd.workflow.flow.bean;

import com.jd.workflow.flow.bean.utils.ValidateUtils;
import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.soap.common.method.MethodMetadata;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.expr.ExprNodeUtils;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;
@Data
public class BeanStepMetadata extends FallbackStepMetadata {
    /**
     * 构造器对应的初始化类
     */
    String initConfigClass;
    /**
     * 构造器对应的初始化配置值
     */
    Object initConfigValue;
    /**
     * 引用的spring bean对象
     */
    String beanName;
    /**
     * 服务类型 1- 工具 2- 服务
     */
    Integer serviceType = null;
    /**
     * 入参
     */
    List<? extends JsonType> input;

    CustomMvelExpression script;

    TaskDefinition taskDef;
    String beanType;
    /**
     * 方法描述
     */
    String desc;
    /**
     * 出参
     */
    JsonType output;

    String methodName;

    public static BeanStepMetadata from(MethodMetadata def) {
        BeanStepMetadata ret = new BeanStepMetadata();
        ret.setInput(def.getInput());
        ret.setOutput(def.getOutput());
        ret.setDesc(def.getDesc());
        ret.setMethodName(def.getMethodName());
        ret.setBeanType(def.getInterfaceName());
        return ret;
    }


    @Override
    public TaskDefinition getTaskDef() {
        return taskDef;
    }

    @Override
    public CustomMvelExpression getSuccessCondition() {
        return null;
    }
    @Override
    public void init(){
        if(!StringUtils.isEmpty(beanName) || serviceType != null){

        }else{
            if(initConfigClass == null) throw new StepParseException("initConfigClass属性不可为空");
            if(initConfigValue == null) throw new StepParseException("initConfigValue不可为空");
            ValidateUtils.validateBeanStepInitArgs(initConfigClass,initConfigValue);
        }

        if(methodName == null)  throw new StepParseException("methodName不可为空");

        MvelUtils.compile(id,"script",script);
        MvelUtils.compileSimpleJsonTypeValue(input,id,"input");
        if(output != null){
            MvelUtils.compileJsonTypeValue(output,id,"output");
        }

    }

    @Override
    public void buildTreeNode(ExprTreeNode parent) {

        ExprTreeNode inputNode = new ExprTreeNode("输入","object",parent.getExpr()+".input");
        parent.addChild(inputNode);

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
