package com.jd.workflow.console.dto;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * http-demon
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HttpMethodDemonModel {
    /**
     * 类型
     */
    String type;
    /**
     * 方法id
     */
    Long methodId;
    /**
     * 环境名称
     */
    String envName;
    /**
     * 输入
     */
    HttpMethodInput input;
    /**
     * 输出
     */
    HttpMethodOutput output;
    /**
     * 成功条件
     */
    String successCondition;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HttpMethodInput{
        /**
         * 请求方式 "GET|POST|OPTIONS|...
         */
        String method;
        /**
         * 请求路径
         */
        String url;
        /**
         * 路径参数demon  List<SimpleJsonType> path;
         */
        Map<String,Object> path;
        /**
         * 参数         List<SimpleJsonType> params;
         */
        Map<String,Object> params;
        /**
         * 表头        List<SimpleJsonType> headers;
         */
        Map<String,Object> headers;
        /**
         * 请求格式 post时候用 form|json
         */
        String reqType;
        /**
         * body       List<JsonType> body;
         */
        Map<String,Object> body;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HttpMethodOutput{
        /**
         * 表头        List<SimpleJsonType> headers;
         */
        Map<String,Object> headers;
        /**
         * body       List<JsonType> body;
         */
        Map<String,Object> body;
    }


    public static HttpMethodDemonModel convertDemonByHttpMethodModel(HttpMethodModel model){
        if(model==null)return null;
        HttpMethodDemonModel demonModel = new HttpMethodDemonModel();
        demonModel.setEnvName(model.getEnvName());
        demonModel.setMethodId(model.getMethodId());
        demonModel.setType(model.getType());
        demonModel.setSuccessCondition(model.getSuccessCondition());
        if(model.input!=null){
            HttpMethodInput input = new HttpMethodInput();
            input.setReqType(model.input.getReqType());
            input.setMethod(model.input.getMethod());
            input.setUrl(model.input.getUrl());
            if(model.input.headers!=null){
                input.setHeaders(model.input.headers.stream().collect(Collectors.toMap(JsonType::getName, JsonType::toDescJson)));
            }
            if(model.input.params!=null){
                input.setParams(model.input.params.stream().collect(Collectors.toMap(JsonType::getName, JsonType::toDescJson)));
            }
            if(model.input.path!=null){
                input.setPath(model.input.path.stream().collect(Collectors.toMap(JsonType::getName, JsonType::toDescJson)));
            }
            if(model.input.body!=null){
                input.setBody(model.input.body.stream().collect(Collectors.toMap(JsonType::getName, JsonType::toDescJson)));
            }
            demonModel.setInput(input);
        }
        if(model.output!=null){
            HttpMethodOutput output = new HttpMethodOutput();
            if(model.output.headers!=null){
                output.setHeaders(model.output.headers.stream().collect(Collectors.toMap(JsonType::getName, JsonType::toDescJson)));
            }
            if(model.output.body!=null){
                output.setBody(model.output.body.stream().collect(Collectors.toMap(JsonType::getName, JsonType::toDescJson)));
            }
            demonModel.setOutput(output);
        }
        return demonModel;
    }
}
