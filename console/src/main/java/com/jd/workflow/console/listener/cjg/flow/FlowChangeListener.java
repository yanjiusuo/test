package com.jd.workflow.console.listener.cjg.flow;

import com.jd.jmq.client.consumer.MessageListener;
import com.jd.jmq.common.message.Message;
import com.jd.workflow.console.entity.requirement.RequirementInfo;
import com.jd.workflow.console.listener.cjg.entity.FlowMessageBody;
import com.jd.workflow.console.listener.cjg.entity.IFlowMessage;
import com.jd.workflow.console.service.model.IApiModelGroupService;
import com.jd.workflow.console.service.requirement.RequirementInfoService;
import com.jd.workflow.console.service.test.RequirementWorkflowService;
import com.jd.workflow.console.service.test.TestRequirementInfoService;
import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class FlowChangeListener implements MessageListener {
    @Autowired
    TestRequirementInfoService testRequirementInfoService;

    @Autowired
    RequirementInfoService requirementInfoService;

    @Autowired
    IApiModelGroupService apiModelGroupService;

    @Override
    public void onMessage(List<Message> messages) throws Exception {
        for (Message message : messages) {
            String text = message.getText();
            FlowMessageBody body = JsonUtils.parse(text, FlowMessageBody.class);
            final IFlowMessage flowMessage = body.toFlowMessage();
            log.info("flow.consume_message:mesage={}", JsonUtils.toJSONString(message));
            if (RequirementWorkflowService.source == flowMessage.getSource()) {
                if (FlowMessageBody.TYPE_MEMBER_CHANGE == body.getMessageType()) {
                    requirementInfoService.syncMember(flowMessage.getFlowId());
                    testRequirementInfoService.syncMembers(flowMessage.getFlowId());

                } else if (FlowMessageBody.TYPE_STATUS_FINISH == body.getMessageType()) {
                    requirementInfoService.syncStatus(flowMessage.getFlowId());
                    //生成模型快照
                    RequirementInfo requirementInfo = requirementInfoService.getByFlowId(flowMessage.getFlowId());
                    apiModelGroupService.saveRequireModelSnapshot(requirementInfo.getId());

                    try {
                        requirementInfoService.updateRequirementMethodStatus(requirementInfo);

                    } catch (Exception ex) {
                        log.error("updateRequirementMethodStatus error", ex);
                    }
                }
                log.info("flow.consume_message:flowId={}", flowMessage.getFlowId());
            } else {
                log.info("flow.ignore_flow_id:flowId={}", text);
            }

        }
    }
}
