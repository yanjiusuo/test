package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;

import java.util.List;

@Data
public class Ws2HttpStepMetadata extends WebServiceBaseStepMetadata {
    /**
     * 请求类型
     */
    ReqType reqType;
    /**
     * http请求方法
     */
    String httpMethod;
    public void init(){
        super.init();
        if(reqType == null){
            throw new StepParseException("webservice.err_miss_req_type").id(id).param("reqType",reqType);
        }
        if(httpMethod == null){
            throw new StepParseException("webservice.err_miss_http_method").id(id).param("reqType",reqType);
        }
    }
}
