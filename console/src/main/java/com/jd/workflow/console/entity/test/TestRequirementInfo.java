package com.jd.workflow.console.entity.test;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.Data;

/**
 * 测试需求信息表: 与deeptest关联
 */
@Data
public class TestRequirementInfo extends BaseEntityNoDelLogic {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 工程id:关联TestProjectInfo
     */
    Long projectId;
    /**
     * 关联类型：1-需求
     */
    int relatedType;
    /**
     * 需求id
     */
    Long requirementId;

    /**
     * 站点信息 test、zh
     */
    String env;


    /**
     * 关联的deeptest模块id
     */
    private Long relatedTestModuleId;

    /**
     * 关联的deeptest用例集合id
     */
    private Long relatedTestCaseGroupId;

}
