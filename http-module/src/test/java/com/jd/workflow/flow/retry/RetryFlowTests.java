package com.jd.workflow.flow.retry;

import com.jd.workflow.BaseTestCase;
import com.jd.workflow.HttpBaseTestCase;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.utils.FlowTestUtils;
import com.jd.workflow.soap.common.util.JsonUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class RetryFlowTests extends HttpBaseTestCase {
    WorkflowInput newWorkflowInput() {
        WorkflowInput input = new WorkflowInput();
        Map<String, Object> params = new HashMap<>();
        params.put("id", 1);
        params.put("name", 10);
        input.setParams(params);
        return input;
    }
    @Test
    public void testRetry(){
        Object result = FlowTestUtils.execFlow(newWorkflowInput(), "classpath:retry/retry-def.json");
        System.out.println(JsonUtils.toJSONString(result));
    }
}
