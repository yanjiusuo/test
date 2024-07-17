package com.jd.workflow.console.dto;

import com.jd.workflow.console.base.PageParam;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

@Data
public class PublicParamsDTO extends PageParam {

    Long id;

    Long interfaceId;

    List<JsonType> content;

    /**
     * 接口类型 1-http、2-webservice、3-jsf 10-编排
     */
    private Integer type;

}
