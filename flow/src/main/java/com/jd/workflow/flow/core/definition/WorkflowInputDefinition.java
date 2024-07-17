package com.jd.workflow.flow.core.definition;

import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.flow.utils.ParametersUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class WorkflowInputDefinition  {
    List<SimpleJsonType> headers;
    List<SimpleJsonType> params;
    ReqType reqType;
    /**
     * body信息，jsonType类型
     */
    List<JsonType> body;
    /**
     * body数据，和body信息只能用一个，入参是json的时候可以用此类型,比如：{"a":1,"b":"2"}，需要根据具体接口来处理
     */
    Object bodyData;
    /**
     * 预处理脚本，可以用来做校验
     */
    CustomMvelExpression preProcess;

    public void init(){
        MvelUtils.compile("input","script",preProcess);
    }

    /**
     * 将值转换为
     * @return
     */
    public WorkflowInput toWorkflowInput(){
        WorkflowInput input = new WorkflowInput();
        ParametersUtils utils = new ParametersUtils();
        Map<String, Object> headers = utils.buildInput(this.headers);
        if(headers == null){
            headers = new HashMap<>();
        }
        input.setHeaders(headers);
        Map<String, Object> params = utils.buildInput(this.params);
        if(params == null){
            params = new HashMap<>();
        }
        input.setParams(params);
        if(bodyData != null){
            input.setBody(bodyData);
        }else if(!CollectionUtils.isEmpty(body)){
            if(ReqType.json.equals(reqType) || reqType == null){ // json
                input.setBody(body.get(0).toExprValue());
            }else{ // form
                input.setBody(utils.buildInput(this.body));
            }

        }

        return input;
    }
}
