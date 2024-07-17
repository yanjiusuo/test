package com.jd.workflow.console.dto.dashboard;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */
@Data
public class RequirementInfoDTO {
    /**
     * 需求名称
     */
    private String name;

    /**
     * 停留时长
     */
    private String stayTime;

    /**
     * 状态
     */
    private String status;
}
