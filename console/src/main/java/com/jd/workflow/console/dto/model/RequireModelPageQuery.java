package com.jd.workflow.console.dto.model;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7 
 */
@Data
public class RequireModelPageQuery  extends PageParam {

    /**
     * 需求id
     */
    private Long requirementId;

    /**
     * 模型名称用于模块检索
     */
    private String modelName;
    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 应用id
     */
    private Long appId;
}
