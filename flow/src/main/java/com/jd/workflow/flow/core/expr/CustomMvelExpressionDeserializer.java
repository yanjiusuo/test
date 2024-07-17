package com.jd.workflow.flow.core.expr;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.apache.commons.lang.StringUtils;
import org.mvel2.compiler.CompiledExpression;

import java.io.IOException;
import java.io.Serializable;

public class CustomMvelExpressionDeserializer extends JsonDeserializer<CustomMvelExpression> {
    @Override
    public CustomMvelExpression deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        String value = p.getValueAsString();
        if(StringUtils.isBlank(value) || isAllComment(value)){
            return null;
        }
        return CustomMvelExpression.mvel(value,false);
    }
    static boolean isAllComment(String value){
        try{
            final CustomMvelExpression mvel = CustomMvelExpression.mvel(value, true);
            final Serializable compiled = mvel.getCompiled();
            if(compiled instanceof CompiledExpression){
                return ((CompiledExpression) compiled).getFirstNode() == null;
            }
            return false;
        }catch (Exception e){
            return false;
        }
    }
    public static void main(String[] args) {
        String value = "//填写示例:\n" +
                " /*\n" +
                "if(workflow.input.params.price < 0){\n" +
                "input.error(\"校验失败：价格无效\");// 会抛出异常，并终止当前执行流程\n" +
                "}\n" +
                "input.attr(\"price\",workflow.input.params.price);\n" +
                "*/ var a = 1;\n" +
                "var b = 2;";
        String value1 = "//填写示例:\n";
        String value2 = "//填写示例:\n" +
                " /*\n" +
                "if(workflow.input.params.price < 0){\n" +
                "input.error(\"校验失败：价格无效\");// 会抛出异常，并终止当前执行流程\n" +
                "}\n" +
                "input.attr(\"price\",workflow.input.params.price);\n" +
                "*/";
        final CustomMvelExpression mvel = CustomMvelExpression.mvel(value, true);
        final Serializable compiled = mvel.getCompiled();
        System.out.println(compiled);
        System.out.println(isAllComment(value));// false
        System.out.println(isAllComment(value1));// true
        System.out.println(isAllComment(value2));// true
    }
}
