package com.jd.workflow.console.service;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.HttpServer;
import com.jd.workflow.console.dto.FlowDebugDto;
import com.jd.workflow.console.dto.FlowDebugResult;
import com.jd.workflow.console.dto.WorkflowTreeBuilderDto;
import com.jd.workflow.console.dto.jsf.JsfDebugDto;
import com.jd.workflow.flow.core.definition.WorkflowInputDefinition;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.soap.common.util.JsonUtils;
import com.jd.workflow.soap.common.util.NetUtils;
import com.jd.workflow.soap.common.xml.schema.expr.ExprTreeNode;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.List;
import java.util.Map;
@Slf4j
public class DebugServiceTests extends BaseTestCase {
    DebugService debugService = new DebugService();

    @Override
    public void setUp() throws Exception {
        HttpServer.run(6010);
        super.setUp();
    }
    @Test
    public void testDebugWorkflow(){
        String content = getResourceContent("classpath:camel/camel-def.json");
        String workflowInput = getResourceContent("classpath:camel/workflow-input.json");
        FlowDebugDto dto = new FlowDebugDto();
        dto.setDefinition(JsonUtils.parse(content, Map.class));
        dto.setInput(JsonUtils.parse(workflowInput, WorkflowInputDefinition.class));
        FlowDebugResult result = debugService.debugFlow(dto,null);
        log.info("input={},output={}",JsonUtils.toJSONString(dto),JsonUtils.toJSONString(result.toMap()));
    }
    @Test
    public void testDebugJsf(){
        String content = getResourceContent("classpath:jsf/jsf-save-person-metadata.json");
        final JsfDebugDto dto = JsonUtils.parse(content, JsfDebugDto.class);
        dto.setUrl("jsf://"+ NetUtils.getLocalHost()+":22000?alias=center");
        final Map output = debugService.debugJsf(dto);
        assertEquals("{\"name\":\"fd\",\"id\":123,\"class\":\"com.jd.workflow.jsf.service.test.Person\",\"age\":123}",JsonUtils.toJSONString(output.get("output")));
        System.out.println(JsonUtils.toJSONString(output));
    }

    @Test
    public void testBuildWorkflowExprTree(){
        String resourceContent = getResourceContent("classpath:definition/all-step-definition.json");
        WorkflowTreeBuilderDto dto = new WorkflowTreeBuilderDto();
        dto.setCurrentStepKey("queryUser1");
        dto.setDefinition(JsonUtils.parse(resourceContent,Map.class));
        List<ExprTreeNode> exprTreeNodes = debugService.buildWorkflowExprTree(dto);

        log.info(":expr_tree_is:{}",JsonUtils.toJSONString(exprTreeNodes));
    }
    @Test
    public void testBuildPseudoCode(){
        String resourceContent = getResourceContent("classpath:definition/all-step-definition.json");
        WorkflowTreeBuilderDto dto = new WorkflowTreeBuilderDto();
        dto.setDefinition(JsonUtils.parse(resourceContent,Map.class));
        String exprCode = debugService.buildPseudoCode(dto.getDefinition());

        log.info("expr_tree_is:{}",exprCode);
    }
}
