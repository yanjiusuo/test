package com.jd.workflow.console.dto.test;

import lombok.Data;
@Data
public class RequirementInterfaceQueryDto {
    /**
     * 需求id
     */
    Long requirementId;
    /**
     * 流程步骤id
     */
    Long stepId;
    /**
     * 接口名称
     */
    String name;
    /**
     * 接口类型 1-http 3-jsf
     */
    Integer type;
    /**
     * 应用id
     */
    Long appId;
    /**
     * 负责人
     */
    String adminCode;
}
