package com.jd.workflow.console.dto.jingme;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/29
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/29 
 */
@Data
public class SendMsgParam {
    /**
     * 消息体
     */
    private TemplateMsgDTO templateMsgDTO;
    /**
     * 接收人erp列表
     */
    private List<String> receiveErps;
}
