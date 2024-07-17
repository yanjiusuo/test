package com.jd.workflow.console.dto.usecase;

import com.jd.workflow.console.dto.EnvModel;
import com.jd.workflow.console.dto.ParamDepDto;
import com.jd.workflow.console.dto.ParamOptDto;
import com.jd.workflow.console.dto.jsf.SsoDto;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-21
 */
@Data
public class HttpDebugDtoSimple {
    /**
     * 方法id
     */
    @NotNull(message = "方法id不可为空")
    String methodId;
    /**
     * 入参
     */
    Input input;
    /**
     * 目的地址，选择环境后对应的地址信息
     */
    String targetAddress;

    EnvModel envModel;

    String site;

    /**
     * 登陆用户，仅支持测试环境
     */
    SsoDto sso;

    /**
     * 环境设置中：自动获取浏览器cookie的值
     */
    Boolean requestCookie = false;


    String envName;

    Boolean isColor;

    /**
     * 前置操作
     */
    List<ParamOptDto> preOpt;

    /**
     * 后置操作
     */
    List<ParamOptDto> postOpt;

    /**
     * 参数依赖
     */
    List<ParamDepDto> paramDep;

    @Data
    public static class Input {
        /**
         * http请求体,json类型的时候可以直接传改数据
         */
        private Object bodyData;
        /**
         * http请求方法
         */
        private String method;

        private String fullUrl;


        /**
         * 路径参数
         */
        List<SimpleJsonType> path;
        /**
         * 请求query参数，jsontype类型
         */
        List<SimpleJsonType> params;
        /**
         * 请求头信息，对象类型
         */
        List<SimpleJsonType> headers;

        List<JsonType> colorHeaders;

        List<JsonType> colorInputParam;
        /**
         * body
         */
        List<JsonType> body;
        /**
         * 请求格式 post时候用 form|json
         */
        String reqType;
        /**
         * 请求地址
         */
        String url;
    }
}
