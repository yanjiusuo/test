package com.jd.workflow.flow.core.metadata.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.jd.workflow.flow.core.definition.TaskDefinition;
import com.jd.workflow.flow.core.enums.ReqType;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.FallbackStepMetadata;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.soap.common.enums.ExprType;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.xml.JsonTypeUtils;
import com.jd.workflow.soap.common.xml.schema.ArrayJsonType;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.ObjectJsonType;
import com.jd.workflow.soap.common.xml.schema.SimpleJsonType;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
@Data
public class WebServiceStepMetadata extends WebServiceBaseStepMetadata {
    @Override
    public void init(){
        super.init();
        if(StringUtils.isEmpty(opName)){
            throw new StepParseException("step.err_op_name_is_required").param("id",id);
        }
    }
}
