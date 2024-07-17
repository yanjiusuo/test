package com.jd.workflow.flow.utils;

import com.jd.workflow.flow.core.attr.AttributeSupport;
import com.jd.workflow.flow.core.exception.*;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.expr.IEvalVarCollector;
import com.jd.workflow.flow.core.step.Step;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.soap.common.enums.ExprType;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import org.apache.camel.ExpressionEvaluationException;
import org.apache.camel.ExpressionIllegalSyntaxException;
import org.mvel2.CompileException;

import java.util.*;

public class MvelUtils {

    static ParametersUtils parametersUtils = new ParametersUtils();
    public static void compile(String stepId,String stage,
                                               CustomMvelExpression expression) throws StepScriptParseException {
        if(expression == null){
            return ;
        }
        try{
             expression.compile();
        }catch (ExpressionIllegalSyntaxException e){

            StepScriptParseException exception = new StepScriptParseException(e);
            Throwable cause = e.getCause();
            exception.setStage(stage);
            if(cause instanceof CompileException){
                CompileException compileException = (CompileException) cause;
                exception.setLine(compileException.getLineNumber());
                exception.setDesc(cause.getMessage());
            }

            exception.id(stepId);

            throw exception;
        }
    }
    public static void compileSimpleJsonTypeValue(List<? extends JsonType> jsonTypes, String stepId, String stage){
       if(jsonTypes == null) return;
        for (JsonType jsonType : jsonTypes) {
            compileJsonTypeValue(jsonType,stepId,stage);
        }
    }
    public static void compileJsonTypeValue(List<? extends JsonType> jsonTypes,String stepId,String stage){
        if(jsonTypes == null) return;
        for (JsonType jsonType : jsonTypes) {
            compileJsonTypeValue(jsonType,stepId,stage);
        }
    }
    public static void compileJsonTypeValue(JsonType jsonType){
        compileJsonTypeValue(jsonType,null,null);
    }
    public static void compileJsonTypeValue(JsonType jsonType,String stepId,String stage){
        if(ExprType.script.equals(jsonType.getExprType())
                && jsonType.getValue()!= null
        ){
            CustomMvelExpression expression = MvelUtils.parse(stepId, stage, (String) jsonType.getValue());
            jsonType.setCompiledValue(expression);
        }
        List<JsonType> children = new ArrayList<>();
        if(jsonType instanceof ArrayJsonType){
            children = ((ArrayJsonType) jsonType).getChildren();
        }else if(jsonType instanceof ObjectJsonType){
            children = ((ObjectJsonType) jsonType).getChildren();
        }
        for (JsonType child : children) {
            compileJsonTypeValue(child,stepId,stage);
        }
    }

        public static CustomMvelExpression parse(String stepId,String stage,String expression) throws StepScriptParseException {
        try{
           return CustomMvelExpression.mvel(expression);
        }catch (ExpressionIllegalSyntaxException e){

            StepScriptParseException exception = new StepScriptParseException(e);
            Throwable cause = e.getCause();
            exception.setStage(stage);
            if(cause instanceof CompileException){
                CompileException compileException = (CompileException) cause;
                exception.setLine(compileException.getLineNumber());
                exception.setDesc(cause.getMessage());
            }

            exception.id(stepId);

            throw exception;
        }
    }

    public static Map<String, Object> getTaskInput(
            Map<String, Object> input,
            ParamMappingContext context,
            AttributeSupport step,
            String stepId
    ) {
        if(context.getStepContext().isDebugMode()){
            EvalContextVars.makeStepVarCollector(step, stepId);
        }
        try{
            return parametersUtils.getTaskInput(input,context);
        }finally {
            if(context.getStepContext().isDebugMode()){
                EvalContextVars.removeCollector();
            }
        }

    }

    /**
     * 映射的时候收集执行时的临时变量，方便调试
     * @param inputParams
     * @param context
     * @param step
     * @param stepId
     * @return
     */
    public static Map<String, Object> getJsonInputValue(
            List<?  extends JsonType> inputParams,
            ParamMappingContext context,
            AttributeSupport step,String stepId
    ) {
        if(context.getStepContext().isDebugMode()){
            EvalContextVars.makeStepVarCollector(step, stepId);
        }
        try{
            return parametersUtils.getJsonInputValue(inputParams,context);
        }finally {
            if(context.getStepContext().isDebugMode()){
                EvalContextVars.removeCollector();
            }
        }

    }
    public static Object getJsonInputValue(
            JsonType input,
            ParamMappingContext context,
            AttributeSupport step,String stepId
    ) {
        if(context.getStepContext().isDebugMode()){
            EvalContextVars.makeStepVarCollector(step, stepId);
        }
        try{
            return parametersUtils.getJsonInputValue(input,context);
        }finally {
            if(context.getStepContext().isDebugMode()){
                EvalContextVars.removeCollector();
            }
        }

    }
    public static Object eval(String stepId, String stage
            , CustomMvelExpression mvelExpression
            , Map<String,Object> args, AttributeSupport support) throws StepScriptParseException {
        try{
            return mvelExpression.evaluate(args, new IEvalVarCollector() {
                @Override
                public void collect(Map<String, Object> variables) {
                    if(support != null){
                        support.getVariables().putAll(variables);
                    }
                }
            });
        }catch (ExpressionEvaluationException e){
            StepScriptEvalException exception = new StepScriptEvalException(stepId,e);
            StepValidateException validateException = getValidateException(exception);
            if(validateException != null){
                StepExecException execException = new StepExecException(stepId, validateException.getMessage());
                execException.setFormatPrams(false);
                throw execException;
            }
            Throwable cause = e.getCause();
            exception.setStage(stage);
            if(cause instanceof CompileException){
                CompileException compileException = (CompileException) cause;
                exception.setLine(compileException.getLineNumber());
                exception.setDesc(cause.getMessage());
            }
            throw exception;
        }
    }
   static StepValidateException getValidateException(Throwable e){
        while (e != null){
            e = e.getCause();
            if(e instanceof StepValidateException){
                return (StepValidateException) e;
            }
        }
        return null;
    }
}
