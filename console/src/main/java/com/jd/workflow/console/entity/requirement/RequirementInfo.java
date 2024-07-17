package com.jd.workflow.console.entity.requirement;

import com.baomidou.mybatisplus.annotation.*;
import com.jd.workflow.console.entity.BaseEntity;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@TableName(value = "requirement_info",autoResultMap = true)
public class RequirementInfo extends BaseEntity {
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_FINISHED = 2;
    public static final int STATUS_NO = 3;
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 关联行云需求编码
     */
    private String relatedRequirementCode;

    /**
     * 需求名称
     */
    private String name;
    /**
     * 需求类型：1-需求交付 2-接口空间
     */
    private Integer type;
    /**
     * 关联id：type=1为工作流使用，type=2为japi使用
     */
    private Long relatedId;
    /**
     * 关联流程模板id
     */
    private String relatedFlowTemplateId;
    /**
     * 状态：1-处理中 2-已完成 3-未启用
     */
    private Integer status;

    /**
     * 空间详情
     */
    private String description;

    /**
     * 部门信息
     */
    private String departmentIds;
    /**
     * 空间名称
     */
    private String spaceName;
    /**
     * 接口空间开放方案名称  需求变动，暂时不做了
     */
    private String opensSolutionName;
    /**
     * 开放类型
     */
    private String openType;
    /**
     * 开放方案描述
     */
    private String openDesc;
    /**
     * 1=开放接口空间
     */
    private Integer shelves;

    /**
     * 代码地址
     */
    private String gitUrl;

    /**
     * 代码分支
     */
    private String gitBranch;


}
