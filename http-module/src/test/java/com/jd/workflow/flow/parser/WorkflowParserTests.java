package com.jd.workflow.flow.parser;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.flow.core.definition.WorkflowDefinition;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.junit.Test;

public class WorkflowParserTests extends BaseTestCase {
    @Test
    public void allStepExtAttrs(){
        String content = getResourceContent("classpath:flow/all-step-ext.json");
        Object map = JsonUtils.parse(content);
        WorkflowDefinition def = WorkflowParser.parse(content);
        assertJsonEquals("{\"tasks\":[{\"id\":\"step1\",\"type\":\"multicast\",\"key\":\"step1\",\"output\":{\"headers\":null,\"body\":[{\"name\":\"root\",\"value\":\"${steps.http1.output.body}\",\"className\":null,\"type\":\"object\",\"children\":[]}],\"script\":null},\"children\":[{\"id\":\"http1\",\"type\":\"http\",\"key\":null,\"input\":{\"url\":\"/json\",\"method\":\"post\",\"reqType\":\"json\",\"params\":null,\"headers\":null,\"path\":null,\"body\":null,\"script\":null,\"preProcess\":null},\"output\":null,\"env\":null,\"endpointUrl\":[\"http://127.0.0.1:6010\"],\"successCondition\":\"output.status==200\",\"taskDef\":null,\"ext1\":\"123\"}],\"ext1\":\"123\"},{\"id\":\"choice\",\"type\":\"choice\",\"key\":\"step2\",\"children\":[{\"key\":\"abc123\",\"when\":\"workflow.input.params.id==1\",\"children\":[{\"id\":\"transform1\",\"type\":\"transform\",\"key\":null,\"output\":{\"headers\":null,\"body\":[{\"name\":\"root\",\"value\":\"condition1\",\"className\":null,\"type\":\"string\"}],\"script\":null}}],\"type\":\"condition\"},{\"key\":\"abc456\",\"when\":\"workflow.input.params.id==1\",\"children\":[{\"id\":\"transform1\",\"type\":\"transform\",\"key\":null,\"output\":{\"headers\":null,\"body\":[{\"name\":\"root\",\"value\":\"condition1\",\"className\":null,\"type\":\"string\"}],\"script\":null}}],\"type\":\"condition\"}],\"ext1\":\"123\"}],\"taskDef\":null,\"input\":{\"headers\":[],\"params\":null,\"reqType\":null,\"body\":null,\"preProcess\":null},\"output\":null,\"failOutput\":null}",JsonUtils.toJSONString(def));
        System.out.println(JsonUtils.toJSONString(def));
    }

    @Test
    public void testFlowParser(){
        String content = getResourceContent("classpath:flow/all-step-ext.json");

        WorkflowDefinition def = WorkflowParser.parse(content);
        System.out.println(JsonUtils.toJSONString(def));
    }
}
