package com.jd.workflow.flow.core.expr;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.EvalContextVars;
import com.jd.workflow.flow.utils.TransformUtils;
import com.jd.workflow.soap.common.exception.StdException;
import com.jd.workflow.soap.common.mapping.CommonParamMappingUtils;
import com.jd.workflow.soap.common.mapping.ICustomParameterMapper;
import org.apache.camel.Exchange;
import org.apache.camel.ExpressionEvaluationException;
import org.apache.camel.ExpressionIllegalSyntaxException;

import org.apache.camel.language.mvel.RootObject;
import org.apache.camel.support.ExpressionSupport;

import org.mvel2.ParserContext;
import org.mvel2.compiler.ExecutableStatement;
import org.mvel2.integration.VariableResolver;
import org.mvel2.integration.impl.CachedMapVariableResolverFactory;
import org.mvel2.integration.impl.CachingMapVariableResolverFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonSerialize(using = CustomMvelExpressionSerializer.class)
@JsonDeserialize(using = CustomMvelExpressionDeserializer.class)
public class CustomMvelExpression extends ExpressionSupport implements ICustomParameterMapper {
    private final String expressionString;
    private final Class<?> type;
    private  Serializable compiled;
    // 工具类列表
    static List<String> utilsClass;
    // java bean引用
    static Map<String,Object> globalVariables;

    private static CachedMapVariableResolverFactory globalVariableResolverFactory;

    public static void setUtilsClass(List<String> list) {
        utilsClass = list;
    }

    public static void setGlobalVariables(Map<String,Object> vars) {
        globalVariables = vars;
        globalVariableResolverFactory = new CachedMapVariableResolverFactory(globalVariables);
    }

    Map<String,Object> defaultImports(){
        Map<String,Object> imports = new HashMap<>();
        imports.put("StepContext", StepContext.class);
        return imports;
    }
    static ParserContext newParserContext(){
        ParserContext ctx = new ParserContext();
        ctx.addPackageImport("java.util");
        ctx.addPackageImport("java.lang");
        ctx.addImport("utils",TransformUtils.class);
        ctx.addPackageImport("com.jd.workflow.flow.core.step");
        ctx.addPackageImport("com.jd.workflow.flow.core.input");
        ctx.addPackageImport("com.jd.workflow.flow.core.output");

        if(utilsClass != null){
            for (String clazz : utilsClass) {
                try{
                    ctx.addImport(Class.forName(clazz));
                }catch (Exception e){
                    throw StdException.adapt(e);
                }
            }
        }

        //ctx.addVariable("utils",TransformUtils.class);


        return ctx;
    }
    public CustomMvelExpression(CustomMvelLanguage language, String expressionString, Class<?> type,boolean compile) {
        this.expressionString = expressionString;
        this.type = type;
        if(compile){
            this.compile();
        }

    }

    public String getExpressionString() {
        return expressionString;
    }

    public Serializable getCompiled() {
        return compiled;
    }

    public CustomMvelExpression(CustomMvelLanguage language, String expressionString, Class<?> type) {
        this(language,expressionString,type,true);
    }
    public void compile(){
        try {
            this.compiled = org.mvel2.MVEL.compileExpression(expressionString,newParserContext());
        } catch (Exception e) {
            throw new ExpressionIllegalSyntaxException(expressionString, e);
        }
    }

    public static CustomMvelExpression mvel(String expression) {
        return mvel(expression,true);
    }

    public static CustomMvelExpression mvel(String expression,boolean compile) {
        return new CustomMvelExpression(new CustomMvelLanguage(), expression, Object.class,compile);
    }

    @Override
    public <T> T evaluate(Exchange exchange, Class<T> tClass) {
        try {
            Object value = org.mvel2.MVEL.executeExpression(compiled, new RootObject(exchange),defaultImports());
            return exchange.getContext().getTypeConverter().convertTo(tClass, value);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(this, exchange, e);
        }
    }

    @Override
    public Object evaluate(Exchange exchange) {
        try {
            return org.mvel2.MVEL.executeExpression(compiled, new RootObject(exchange));
        } catch (Exception e) {
            throw new ExpressionEvaluationException(this, exchange, e);
        }
    }

    public Object evaluate(Map  args) {
       return evaluate(args,null);
    }
    public Object evaluate(Map  args,IEvalVarCollector collector) {
        CachingMapVariableResolverFactory factory = new CachingMapVariableResolverFactory(args);
        if(globalVariableResolverFactory != null){
            factory.setNextFactory(globalVariableResolverFactory);
        }

        try {////return org.mvel2.MVEL.executeExpression(compiled, args);
            EvalContextVars.setVars(args);
            return ((ExecutableStatement) compiled).getValue(null, factory);
        } catch (Exception e) {
            throw new ExpressionEvaluationException(this, null, e);
        }finally {
            EvalContextVars.removeVars();
            factory.externalize();
            Map<String,Object> vars = collectNewVars(factory);
            if(collector != null){
                collector.collect(vars);
            }
            if(EvalContextVars.getVarCollector() != null){
                EvalContextVars.getVarCollector().collect(vars);
            }
        }
    }
     static Map<String,Object> collectNewVars(CachingMapVariableResolverFactory factory){
         Map<String,Object> variables = new HashMap<>();
         for (Map.Entry<String, VariableResolver> entry : factory.getVariableResolvers().entrySet()) {
             if (entry.getValue().getFlags() == -1) variables.put(entry.getKey(), entry.getValue().getValue());
         }
         return variables;
     }
    public Class<?> getType() {
        return type;
    }

    @Override
    protected String assertionFailureMessage(Exchange exchange) {
        return expressionString;
    }

    @Override
    public String toString() {
        return "Mvel[" + expressionString + "]";
    }

    @Override
    public Object evaluate(CommonParamMappingUtils utils, CommonParamMappingUtils.EvalContext context) {
        return evaluate(context.getArgs());
    }
}
