package com.jd.workflow.console.dto.flow.param;

import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 15:44
 * @Description:
 */
@Data
public class FlowParamGroupDTO {

    /**
     * 分组id
     */
    private Long id;

    /**
     * 分组名称
     */
    private String groupName;
}
