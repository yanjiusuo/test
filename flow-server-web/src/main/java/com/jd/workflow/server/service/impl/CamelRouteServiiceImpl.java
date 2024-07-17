package com.jd.workflow.server.service.impl;

import com.jd.businessworks.plugins.PluginsClient;
import com.jd.workflow.flow.core.input.WorkflowInput;
import com.jd.workflow.flow.core.output.HttpOutput;
import com.jd.workflow.flow.core.output.Output;
import com.jd.workflow.flow.core.step.StepContext;
import com.jd.workflow.server.service.CamelRouteServiice;
import com.jd.workflow.soap.common.util.StringHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CamelRouteServiiceImpl implements CamelRouteServiice {
    @Override
    public HttpOutput execute(String id, WorkflowInput workflowInput) {
        // 健康必须设置 WorkflowInput，否则使用传统的流程编排
        if (workflowInput == null) {
            throw new IllegalArgumentException("WorkflowInput  不能为 null");
        }
        id = StringHelper.replace(id,"/","$");
        PluginsClient.PluginParam pluginParam = new PluginsClient.PluginParam(id, id);
        pluginParam.setWorkflowInput(workflowInput);

        Output output = (Output) PluginsClient.send(pluginParam);
        HttpOutput httpOutput = null;
        if(output instanceof HttpOutput){
            httpOutput = (HttpOutput) output;
        }else{
            httpOutput = new HttpOutput();
            httpOutput.setBody(output.getBody());
            httpOutput.setException(output.getException());
            httpOutput.setSuccess(output.isSuccess());
            httpOutput.setStatus(200);
        }

        return httpOutput;
    }
}
