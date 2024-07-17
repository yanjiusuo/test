package com.jd.workflow.console.dto;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

/**
 *webservice输出dto
 */
@Data
public class WebServiceOutputDTO {

    private String demoXml;

    private JsonType schemaType;

    /**
     * response header
     */
    private List<JsonType> headers;

    /**
     * response body
     */
    private List<JsonType> body;
}
