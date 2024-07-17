package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.utils.JsonValidateUtils;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;

import java.util.List;

@Data
public class OutputCollectMetadata {
    List<SimpleJsonType> headers;
    List<JsonType> body;
    CustomMvelExpression script;
    public void compile(String stepId,String stage){
        if(getScript()!=null){
            MvelUtils.compile(stepId,stage+".script",getScript());
        }else{
            MvelUtils.compileJsonTypeValue(headers,stepId,stage+".headers");
            MvelUtils.compileJsonTypeValue(body,stepId,stage+".body");

            JsonValidateUtils.validateHttpBody(getBody(),stepId);
        }
    }
}
