package com.jd.workflow.console.dto;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

/**
 * 项目名称：example
 * 类 名 称：WebServiceInputDto
 * 类 描 述：webservice输入dto
 * 创建时间：2022-05-26 20:40
 * 创 建 人：wangxiaofei8
 */
@Data
public class WebServiceInputDTO {

    private String demoXml;

    private JsonType schemaType;

    /**
     * reuest parmas
     */
    private List<JsonType> params;

    /**
     * reuest headers
     */
    private List<JsonType> headers;
    private List<JsonType> path;

    /**
     * reuest body
     */
    private List<JsonType> body;

    /**
     * 请求体body时 form  or json
     */
    String reqType;


    /*
    String name;

    Object value;

    String className;

    String desc;

    boolean required;

    String type;

    List<JsonType> children = new LinkedList<>();
    */

}
