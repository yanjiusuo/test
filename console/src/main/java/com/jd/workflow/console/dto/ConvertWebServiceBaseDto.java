package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.Date;

/**
 *webservice基本类
 */
@Data
public class ConvertWebServiceBaseDto {

    private Long id;

    private Long interfaceId;

    private Long methodId;

    private String reqType = "post";

    private String callEnv;

    /**
     * 0 未发布 1 发布
     */
    private Integer published;

    private Date modified;

    /**
     * 转换后的webservice-wsdlUrlPath
     */
    private String mockWsdlUrlPath;

}
