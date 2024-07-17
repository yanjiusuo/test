package com.jd.workflow.console.entity.method;

import com.jd.workflow.console.dto.HttpMethodModel;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.soap.common.util.ObjectHelper;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import lombok.Data;

import java.util.List;

@Data
public class JsfDeltaInfo extends DeltaInfo{
    List<? extends JsonType> input;

    /**
     * 出参
     */
    JsonType output;

    @Override
    public boolean isEmpty() {
        return deltaAttrs.isEmpty()
                && ObjectHelper.isEmpty(input)
                && ObjectHelper.isEmpty(output);
    }
}
