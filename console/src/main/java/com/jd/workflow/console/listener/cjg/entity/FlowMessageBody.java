package com.jd.workflow.console.listener.cjg.entity;

import com.jd.workflow.soap.common.util.JsonUtils;
import lombok.Data;

import java.util.Map;

@Data
public class FlowMessageBody {
    public static final int TYPE_CREATE = 1;
    public static final int TYPE_DELETE = 2;
    public static final int TYPE_MEMBER_CHANGE = 3;
    public static final int TYPE_STATUS_FINISH = 5;
    Integer messageType;
    Map<String,Object> messageBody;

    public <T extends IFlowMessage> T toFlowMessage() {
        if(messageType == TYPE_CREATE) {
            return (T) JsonUtils.parse(JsonUtils.toJSONString(messageBody), WFFlowInstanceCreateMessageBody.class);
        } else {
            return (T) JsonUtils.parse(JsonUtils.toJSONString(messageBody), WFlowChangeMessageBody.class);
        }
    }

}
