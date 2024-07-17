package com.jd.workflow.console.dto;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/24
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/24 
 */
@Data
public class ModelGroupDTO {

    /**
     * 分组id
     */
    private Long groupId;
    /**
     * 分组名称
     */
    private String name;
    /**
     * 分组英文名称
     */
    private String enName;

    /**
     * 应用id
     */
    private  Long appId;

    /**
     * 父节点
     */
    private Long parentId;
}
