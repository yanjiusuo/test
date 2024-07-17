package com.jd.workflow.console.dto;

import com.jd.workflow.soap.common.method.ColorGatewayParamDto;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HttpMethodModel {
    /**
     * 类型
     */
    String type;

    String methodCode;
    /**
     * 方法id
     */
    Long methodId;
    /**
     * 环境名称
     */
    String envName;
    List<String> authKeys;
    /**
     * 输入
     */
    HttpMethodInput input;
    /**
     * 输出
     */
    HttpMethodOutput output;


    /**
     * 输出
     */
    ColorGatewayParamDto colorInput;


    /**
     * 输出
     */
    ColorGatewayParamDto colorOutput;
    /**
     * 成功条件
     */
    String successCondition;

    String desc;
    String summary;

    public void initEmptyValue(){
        if(input!=null){
            if(input.getPath() == null){
                input.setPath(new ArrayList<>());
            }
            if(input.getParams() == null){
                input.setParams(new ArrayList<>());
            }
            if(input.getHeaders() == null){
                input.setHeaders(new ArrayList<>());
            }

        }
        if(output!=null){
            if(output.getHeaders() == null){
                output.setHeaders(new ArrayList<>());
            }

        }
    }
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
         * 路径参数
         */
        List<SimpleJsonType> path;
        /**
         * 参数
         */
        List<JsonType> params;
        /**
         * 表头
         */
        List<JsonType> headers;
        /**
         * 请求格式 post时候用 form|json
         */
        String reqType;
        /**
         * body
         */
        List<JsonType> body;
    }
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class HttpMethodOutput{
        /**
         * 表头
         */
        List<JsonType> headers;
        /**
         *
         */
        List<JsonType> body;
    }
}
