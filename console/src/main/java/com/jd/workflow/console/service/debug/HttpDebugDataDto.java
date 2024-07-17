package com.jd.workflow.console.service.debug;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.console.dto.jsf.HttpDebugDto;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.output.ExceptionSerializer;
import lombok.Data;

import java.util.Map;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-06-18 修改 增加type 前端需要知道是不是http请求
 */
@Data
public class HttpDebugDataDto {
    Input input;
    Output output;
    String site;
    String desc;

    // todo 冗余
    /**
     * http jsf
     */
    String type;
    /**
     * 初始入参
     */
    HttpDebugDto dto;
    @Data
    public static class Input {
        /**
         * url前缀
         */
        String targetAddress;
        /**
         * http请求路径
         */
        String url;
        Object body;
        Map<String,Object> params;

        Map<String,Object> headers;
        Map<String,Object> path;
        Map<String,Object> colorHeaders;
        Map<String,Object> colorInPutParam;

        // todo 冗余
        /**
         * http方法
         */
        String method;
        /**
         * form、xml、json
         */
        ReqType reqType;
        /**
         * 内容类型
         */
        String contentType;
    }
    @Data
    public static class Output {
        Object body;
        Map<String,Object> headers;
        @JsonSerialize(using = ExceptionSerializer.class)
        Exception exception;
    }
}
