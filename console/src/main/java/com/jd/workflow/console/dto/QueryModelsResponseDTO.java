package com.jd.workflow.console.dto;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/27
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/27
 */
@Data
public class QueryModelsResponseDTO {
    /**
     * id
     */
    Long id;
    /**
     * 名称
     */
    String name;
    /**
     * 应用id
     */
    Long appId;
    /**
     * 描述
     */
    String desc;
    /**
     * 是否自动上报
     */
    Integer autoReport;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 分组名称
     */
    private String groupName;
}
