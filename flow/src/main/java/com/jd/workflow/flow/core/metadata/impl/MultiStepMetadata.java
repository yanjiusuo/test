package com.jd.workflow.flow.core.metadata.impl;

import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.soap.common.xml.schema.JsonType;
import com.jd.workflow.soap.common.xml.schema.expr.ExprNodeUtils;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
public class MultiStepMetadata extends CollectStepMetadata {
    List<StepDefinition> children;

    public void init(){
        super.init();

    }

    @Override
    public String buildPseudoCode() {
        String template = "{//并行执行\n%s\n}\n";
        StringBuilder sb = new StringBuilder();
        String childCode =children.stream().map(vs->vs.getMetadata().buildPseudoCode()).collect(Collectors.joining("\n"));
        sb.append(childCode);
        sb.append("\n");
        String aggTemplate = "collectAggOutput({\"stepId\":\"%s\"});";

        sb.append(String.format(aggTemplate,getId()));
        return String.format(template,sb.toString());
    }


}
