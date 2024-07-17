package com.jd.workflow.flow.core.definition;

import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Data
public class WorkflowOutputDefinition {
    List<SimpleJsonType> headers;
    List<JsonType> body;
    CustomMvelExpression script;
    public void init(){
        MvelUtils.compile("input","script",script);
    }

    public boolean isEmpty(){
        return script == null && CollectionUtils.isEmpty(headers) && CollectionUtils.isEmpty(body);
    }
}
