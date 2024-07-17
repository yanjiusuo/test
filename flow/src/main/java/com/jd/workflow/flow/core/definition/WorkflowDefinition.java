package com.jd.workflow.flow.core.definition;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.jd.workflow.flow.core.definition.serializer.WorkflowDefinitionDeserializer;
import com.jd.workflow.flow.core.definition.serializer.WorkflowDefinitionSerializer;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.flow.parser.StepTopoGraph;
import com.jd.workflow.soap.common.xml.schema.JsonTypeDeserializer;
import com.jd.workflow.soap.common.xml.schema.JsonTypeSerializer;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@JsonDeserialize(using = WorkflowDefinitionDeserializer.class)
//@JsonSerialize(using = WorkflowDefinitionSerializer.class)
@Data
public class WorkflowDefinition {
    List<StepDefinition> tasks = new ArrayList<>();
    List<WorkflowParam> params;
    /**
     * 是否有java bean节点，有java bean节点的服务需要用户本地启动
     */
    boolean hasJavaBean = false;
    TaskDefinition taskDef;
    WorkflowInputDefinition input;
    WorkflowOutputDefinition output;
    WorkflowOutputDefinition failOutput;

    /**
     * step1是否比step2更前
     * @param step1
     * @param step2
     * @return
     */
    public boolean isBefore(String step1,String step2){
        if(step1 == null || step2 == null) return false;
        List<String> sortStepIds = new ArrayList<>();

        for (StepDefinition task : tasks) {
            traverse(task,sortStepIds);
        }
        if(sortStepIds.indexOf(step1) == -1){
            return false;
        }
        return sortStepIds.indexOf(step1) <= sortStepIds.indexOf(step2);
    }
    private void traverse(StepDefinition definition, List<String> list){


        for (Map.Entry<Integer, List<StepDefinition>> entry : definition.getChildren().entrySet()) {
            for (StepDefinition stepDefinition : entry.getValue()) {
                traverse(stepDefinition,list);
            }
        }
        list.add(definition.getMetadata().getKey());
    }

    public void init(){
        if(input != null){
            input.init();
        }
        if(output != null){
            output.init();
        }
        if(failOutput!=null){
            failOutput.init();
        }

    }


}
