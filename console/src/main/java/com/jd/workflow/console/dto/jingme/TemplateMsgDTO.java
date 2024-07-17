package com.jd.workflow.console.dto.jingme;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/27
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/27 
 */
@Data
public class TemplateMsgDTO {

    /**
     * 标题
     */
    private String head;

    /**
     * 卡片头副标题信息
     */
    private String subHeading;

    /**
     * 卡片头副标题@的用户信息
     */
    private List<UserDTO> atUsers;

    /**
     * 主内容上描述
     */
    private List<CustomDTO> customFields;

    /**
     * 卡片内容描述
     */
    private String content;

    /**
     *
     */
    private List<ButtonDTO> buttons;
}
