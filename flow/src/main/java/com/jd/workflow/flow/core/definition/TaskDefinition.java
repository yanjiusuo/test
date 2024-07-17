package com.jd.workflow.flow.core.definition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.expr.CustomMvelExpressionDeserializer;
import com.jd.workflow.flow.core.expr.CustomMvelExpressionSerializer;
import com.jd.workflow.flow.core.retry.RetryConfig;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Data
public class TaskDefinition {
    private int timeout=5000;
    FallbackStrategy fallbackStrategy;
    Fallback fallback;
    /**
     * 休眠时长
     */
    Integer delayTime;

    RetryConfig retryConfig;

    public void init(){
        if(delayTime != null && delayTime > 60*1000){
            throw new StepParseException("step.err_sleep_time_is_over_one_minute");
        }
    }

    @JsonSerialize(using = FallbackStrategySerializer.class)
    @JsonDeserialize(using = FallbackStrategyDeserializer.class)
    public static enum  FallbackStrategy{
        STOP,CONTINUE,RETRY;
    }

    @JsonSerialize(using = FallbackSerializer.class)
    @JsonDeserialize(using = FallbackDeserializer.class)
    @Data
    public static class Fallback{
        /**
         * 原始值
         */
        Object original;
        /**
         * 对象值
         */
        Object value;
    }
    public static class FallbackSerializer extends JsonSerializer<Fallback> {

        @Override
        public void serialize(Fallback value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeObject(value.getOriginal());
        }
    }

    public static class FallbackDeserializer extends JsonDeserializer<Fallback> {

        @Override
        public Fallback deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            Fallback fallback = new Fallback();
            JsonNode treeNode = p.readValueAsTree();

            if(treeNode.isObject()  || treeNode.isArray()){
                Class clazz = Map.class;
                if(treeNode.isArray()){
                    clazz = List.class;
                }
                Object map = JsonUtils.mapper().treeToValue(treeNode, clazz);
                fallback.original = map;
                fallback.value = map;
                return fallback;
            }
            String value = treeNode.asText();
            fallback.setOriginal(value);
            if(!StringUtils.isEmpty(value)){

                try{
                    fallback.setValue(JsonUtils.parse(value));
                }catch (Exception e){
                    fallback.setValue(value);
                }



            }
            return fallback;
        }
    }


    public static class FallbackStrategySerializer extends JsonSerializer<FallbackStrategy> {

        @Override
        public void serialize(FallbackStrategy value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.name().toLowerCase());
        }
    }

    public static class FallbackStrategyDeserializer extends JsonDeserializer<FallbackStrategy> {

        @Override
        public FallbackStrategy deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
             String value = p.getValueAsString().trim();
             return FallbackStrategy.valueOf(value.toUpperCase());
        }
    }

}
