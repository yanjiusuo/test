package com.jd.workflow.console.dto;

import com.jd.workflow.flow.core.definition.WorkflowInputDefinition;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.soap.common.xml.schema.ComplexJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleParamType;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class FlowDebugDto {
    Map<String,Object> definition;
    WorkflowInputDefinition input;
    Long methodId;

    /**
     * 调试的时候将空对象改为空串，前端未做这块的处理，现在这块做一下
     */
    public void replaceNullStringToEmptyString(){
        replaceNullStringToEmptyString(input.getHeaders());
        replaceNullStringToEmptyString(input.getParams());
        replaceNullStringToEmptyString(input.getBody());
    }
    private static void replaceNullStringToEmptyString(List<? extends JsonType> jsonTypes){
        if(jsonTypes == null) return;
        for (JsonType jsonType : jsonTypes) {
            replaceNullStringToEmptyString(jsonType);
        }
    }
    private static void replaceNullStringToEmptyString(JsonType jsonType){
        if(jsonType == null) return;
        if(jsonType.isSimpleType() && SimpleParamType.STRING.typeName().equals(jsonType.getType())){
            if(jsonType.getValue() == null){
                jsonType.setValue("");
            }
        }else if(jsonType instanceof ComplexJsonType){
            for (JsonType child : ((ComplexJsonType) jsonType).getChildren()) {
                replaceNullStringToEmptyString(child);
            }
        }
    }
}
