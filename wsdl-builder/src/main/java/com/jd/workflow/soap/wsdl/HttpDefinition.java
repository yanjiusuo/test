package com.jd.workflow.soap.wsdl;

import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;

import java.util.List;

/**
 * http接口定义
 */
@Data
public class HttpDefinition {
    static final String REQ_TYPE_FORM = "form";
    static final String REQ_TYPE_JSON = "json";
    /**
     * http的目标地址
     */
    String url;
    /**
     * http转webservice后的实际调用地址
     */
    String webServiceCallUrl;
    List<? extends JsonType> params;
    List<? extends JsonType> headers;
    List<? extends JsonType> path;
    List<? extends JsonType> respHeaders;
    List<JsonType> respBody;
    String reqType;
    List<JsonType> body;

    String serviceName;

    String pkgName;


    String methodName;

}
