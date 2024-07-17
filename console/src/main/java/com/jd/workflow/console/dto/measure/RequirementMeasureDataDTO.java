package com.jd.workflow.console.dto.measure;

import lombok.Data;

import java.util.Date;

/**
 * @author yza
 * @description
 * @date 2024/1/16
 */
@Data
public class RequirementMeasureDataDTO {

    /**
     * 需求名称
     */
    private String name;

    /**
     * 部门
     */
    private String department;

    /**
     * 关联行云需求编码
     */
    private String relatedRequirementCode;

    /**
     * 关联流程模板id
     */
    private String relatedFlowTemplateId;

    /**
     * 状态：处理中 已完成 未启用
     */
    private String statusName;

    /**
     * 创建人
     */
    private String creator;

    /**
     * 创建时间
     */
    private Date created;
}
