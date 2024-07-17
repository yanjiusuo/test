package com.jd.workflow.flow.core.input;

import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.exception.StepValidateException;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;
import java.util.Map;
@Data
public class HttpInput extends BaseInput{
    /**
     * http方法
     */
    String method;
    /**
     * form、xml、json
     */
    ReqType reqType;
    /**
     * 内容类型
     */
    String contentType;

    private Object body;
    private Map<String,Object> headers;
    private Map<String,Object> params;
    String url;




}
