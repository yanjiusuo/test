package com.jd.workflow.console.base.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author yza
 * @description:
 * @date 2024/1/12
 */
@AllArgsConstructor
public enum MeasureDataEnum {

    /**
     *
     */
    REPORT_HTTP(1, "http接口上报"),

    /**
     *
     */
    REPORT_JSF(2, "jsf接口上报"),

    /**
     *
     */
    QUICK_CALL_HTTP(3, "快捷调用(http)"),

    /**
     *
     */
    INTERFACE_DOC_DETAIL(4, "接口文档浏览"),

    /**
     *
     */
    PLUGIN_DOWNLOAD(5, "插件下载"),

    /**
     *
     */
    MOCK_JSF_DEFAULT_TEMPLATE(6, "mock(jsf默认模版)"),

    /**
     *
     */
    MOCK_HTTP_DEFAULT_TEMPLATE(7, "mock(http默认模版)"),

    /**
     *
     */
    QUICK_CALL_MOCK_JSF_TEMPLATE(8, "快捷调用一键mock模版（jsf）"),

    /**
     *
     */
    QUICK_CALL_MOCK_HTTP_TEMPLATE(9, "快捷调用一键mock模版（http）"),

    /**
     *
     */
    QUICK_CALL_JSF(10, "快捷调用(jsf)"),

    ;

    /**
     *
     */
    @Getter
    @Setter
    private Integer code;

    /**
     *
     */
    @Getter
    @Setter
    private String description;

    public static MeasureDataEnum getByCode(Integer code){
        for (MeasureDataEnum value : MeasureDataEnum.values()) {
            if(value.getCode().equals(code)){
                return value;
            }
        }
        return null;
    }
}
