package com.jd.workflow.console.dto.model;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/26
 */

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/7/26
 */
@Data
public class ApiModelPageQuery extends PageParam {
    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 应用id
     */
    private Long appId;

    /**
     * 应用名称模糊查询
     */
    private String modelName;

}
