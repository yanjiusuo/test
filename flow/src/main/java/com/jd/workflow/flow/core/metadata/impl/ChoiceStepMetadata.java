package com.jd.workflow.flow.core.metadata.impl;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.jd.workflow.flow.core.definition.StepDefinition;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.core.expr.CustomMvelExpression;
import com.jd.workflow.flow.core.metadata.StepMetadata;
import com.jd.workflow.flow.utils.MvelUtils;
import com.jd.workflow.soap.common.xml.XNode;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.Data;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
public class ChoiceStepMetadata extends StepMetadata {
    List<ChoiceStep> children;
    public void init(){

        for (int i = 0; i < children.size(); i++) {
            ChoiceStep step = children.get(i);
            String stepName = "分支"+(i+1);
            if(i< children.size()-1){
                if(step.when == null || StringUtils.isEmpty(step.when.getExpressionString())){
                    throw new StepParseException("choice.err_when_is_required").id(this.id).param("stage",stepName);
                }
            }
            MvelUtils.compile(this.id,stepName,step.getWhen());
        }
    }

    @Override
    public void buildTreeNode(ExprTreeNode parent) {

    }

    @Override
    public String buildPseudoCode() {
        String ifExpr = "if(%s){\n%s\n}";
        String elseIfExpr = "else if(%s){\n%s\n}";
        String elseExpr = "else{\n%s\n}";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < children.size(); i++) {
            ChoiceStep condition = children.get(0);
            String childCode = condition.getChildren().stream().map(vs->vs.getMetadata().buildPseudoCode()).collect(Collectors.joining("\n"));
            if(i==0){
                sb.append(String.format(ifExpr,condition.getWhen().getExpressionString(),childCode));
            }else if(i==children.size() - 1){
                sb.append(String.format(elseExpr,childCode));
            }else{
                sb.append(String.format(elseIfExpr,condition.getWhen().getExpressionString(),childCode));
            }

        }
        return sb.toString();
    }

    @Data
    public static class ChoiceStep {
        String key;
        CustomMvelExpression when;
        List<StepDefinition> children;
        Map<String,Object> anyAttrs;
        @JsonAnyGetter
        public Map<String, Object> getAnyAttrs() {
            return anyAttrs;
        }
        @JsonAnySetter
        public void setAnyAttrs(Map<String, Object> anyAttrs) {
            this.anyAttrs = anyAttrs;
        }

    }
}
