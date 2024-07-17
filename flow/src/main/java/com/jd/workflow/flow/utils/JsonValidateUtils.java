package com.jd.workflow.flow.utils;

import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.exception.StepReqValidateException;
import com.jd.workflow.soap.common.exception.JsonTypeParseError;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;

import java.util.List;
import java.util.Map;

public class JsonValidateUtils {
   public static void parseJsonType(){

   }
    private static void validateJsonType(JsonType jsonType){
        if(jsonType.isRequired()){
            if(jsonType.getValue() == null){
                throw new JsonTypeParseError("jsontype.err_miss_required_value")
                        .param("level", jsonType.getName());
            }
        }
    }

    public static void validate(String stepId,String stage,List<SimpleJsonType> jsonTypes,Map<String,Object> values){
       if(jsonTypes == null) return;
        for (SimpleJsonType jsonType : jsonTypes) {
            if(jsonType.isRequired()
                    && (values == null || values.get(jsonType.getName()) == null)
            ){
                throw new StepReqValidateException(stepId,"input.err_miss_required_param")
                        .param("stage",stage)
                        .param("name",jsonType.getName());
            }
        }
    }

    public static void validateHttpBody(List<JsonType> body,String id){
        if(body == null || body.isEmpty()){
            return;
           // throw new StepParseException("step.err_body_root_is_required");
        }
        JsonType jsonType = body.get(0);
        /*if( !"root".equalsIgnoreCase(jsonType.getName())){ //!(jsonType instanceof ObjectJsonType) ||
            throw new StepParseException("httpstep.err_body_root_is_required").id(id);
        }*/
    }
    public static JsonType getHttpBody(List<JsonType> body){
        if(body == null || body.isEmpty()){
            return null;
        }
        return body.get(0);
    }

}
