package com.jd.workflow.flow.parser;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.flow.core.exception.ErrorMessageFormatter;
import com.jd.workflow.flow.core.exception.StepParseException;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class WorkflowParserTests extends BaseTestCase {
    @Test
    public void testChoiceDuplicateId() {
        String content = getResourceContent("classpath:flow/branch-duplicate-id.json");
        WorkflowParser.parse(content);
    }

    @Test
    public void testMulticastDuplicateId() {
        try {
            String content = getResourceContent("classpath:flow/multicast-duplicate-id.json");
            WorkflowParser.parse(content);
        } catch (Exception e) {

            assertTrue(e instanceof StepParseException);
           String msg =  ErrorMessageFormatter.formatMsg((StepParseException)e);
            System.out.println(msg);
        }

    }

    @Test
    public void testWorkflowSerializer() {
        String content = getResourceContent("classpath:flow/branch-duplicate-id.json");
        WorkflowDefinition def = WorkflowParser.parse(content);
        System.out.println(JsonUtils.toJSONString(def));

    }
    @Test
    public void testWorkflowParserNoValidate() {
        String content = getResourceContent("classpath:flow/error-flow-step.json");
        WorkflowDefinition def = WorkflowParser.parse(JsonUtils.parse(content, Map.class),false,true);
        System.out.println(JsonUtils.toJSONString(def));

    }


}
