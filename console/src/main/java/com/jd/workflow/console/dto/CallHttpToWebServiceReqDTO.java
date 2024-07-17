package com.jd.workflow.console.dto;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * http转webservice方法调试
 */
@Data
public class CallHttpToWebServiceReqDTO {

    /**
     * 转webservice方法后主键
     */
    @NotNull(message = "http转webservice的方法id不能为空")
    private Long methodId;

    /**
     * http接口对应的id
     */
    @NotNull(message = "接口Id不能为空")
    private Long interfaceId;
    /**
     * 调用环境名称
     */
    private String envName;

    /**
     * 调用地址 发布后才能调用 调试时使用
     */
    private String endpointUrl;

    /**
     * 请求参数,当是xml是为字符串，
     * 当非xml时为JsonType对象
     *
     */
    @NotNull(message = "请求参数不能为空")
    private Object input;

    /**
     * 请求类型
     */
    @NotNull(message = "请求参数类型不能为空,xml或者json")
    private String inputType;
    /*@Data
    public static class WebServiceDebugInput{
        List<JsonType> header;
        ObjectJsonType body;
    }*/
}
