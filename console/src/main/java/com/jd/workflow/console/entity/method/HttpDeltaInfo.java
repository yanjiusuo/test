package com.jd.workflow.console.entity.method;

import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.soap.common.util.ObjectHelper;
import lombok.Data;

@Data
public class HttpDeltaInfo extends DeltaInfo{
    HttpMethodModel.HttpMethodInput input;
    HttpMethodModel.HttpMethodOutput output;

    private boolean isEmptyInput(){
        return ObjectHelper.isEmpty(input.getHeaders())
                &&ObjectHelper.isEmpty(input.getBody())
                &&ObjectHelper.isEmpty(input.getParams())
                ;
    }
    private boolean isEmptyOutput(){
        return ObjectHelper.isEmpty(output.getHeaders())
                &&ObjectHelper.isEmpty(output.getBody())
                ;
    }
    @Override
    public boolean isEmpty() {

        return deltaAttrs.isEmpty()
                && isEmptyInput() && isEmptyOutput()
                ;
    }
}
