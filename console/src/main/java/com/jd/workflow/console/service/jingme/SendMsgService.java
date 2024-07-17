package com.jd.workflow.console.service.jingme;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/27
 */

import com.jd.workflow.console.dto.jingme.TemplateMsgDTO;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/27 
 */
public interface SendMsgService {

    /***
     * 获取team token
     * @return
     */
    String getTeamAccToken();

    /**
     * 获取app token
     * @return
     */
    String getAppToken();

    /**
     * 发给用户互动卡片消息
     * @param erp
     * @param templateMsgDTO
     * @return
     */
    String sendUserJueMsg(String erp, TemplateMsgDTO templateMsgDTO);

    /**
     *
     */
    void delTeamToken();
}
