package com.jd.workflow.console.dto.flow.param;

import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 15:57
 * @Description:
 */
@Data
public class FlowParamDTO {
    /**
     * 参数id
     */
    private Long id;

    /**
     * 参数名称
     */
    private String name;

    /**
     * 参数值
     */
    private String value;

    /**
     * 分组id
     */
    private Long groupId;

    /**
     * 分组名称(只有分组名称无分组id时，自动创建分组)
     */
    private String groupName;

    /**
     * 参数描述
     */
    private String description;

}
