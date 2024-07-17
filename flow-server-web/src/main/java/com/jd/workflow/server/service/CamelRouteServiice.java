package com.jd.workflow.server.service;

import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;

public interface CamelRouteServiice {
    HttpOutput execute(String id, WorkflowInput workflowInput);
}
