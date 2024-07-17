package com.jd.workflow.console.dto;

import lombok.Data;

/**
 * @description:前置后置操作-参数依赖-设置变量
 * @author: sunchao81
 * @Date: 2024-05-21
 */
@Data
public class ParamDepDto {

    /**
     *
     */
    private String key;

    /**
     *
     */
    private String value;

    /**
     *
     */
    private String desc;


    /**
     * 依赖信息解析
     */
    class DepParse {
        /**
         * 10-前置|2X-方法|30-后置
         */
        private String position;

        /**
         * 物料模版|用例|其他
         */
        private String typeName;

        /**
         * wlTool|case|other
         */
        private String type;

        /**
         * 关联ID-物料模版使用
         */
        private Long referId;

        /**
         * 入参还是出参
         */
        private String paramType;

        /**
         * jsonPath参数
         */
        private Object jsonPath;
    }



}
