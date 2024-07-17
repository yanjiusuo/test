package com.jd.workflow.console.entity.test;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.jd.workflow.console.entity.BaseEntity;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.Data;

import java.util.List;

@Data
@TableName(value = "test_cases_execute_info", autoResultMap = true)
public class TestCasesExecuteInfo extends BaseEntityNoDelLogic {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 关联deeptest用例id
     */
    String testExecuteId;
    String env;
    /**
     * 关联需求id
     */
    Long relatedRequirementId;
    /**
     * 关联步骤id
     */
    String relatedFlowStepId;
    /**
     * 执行用例集合
     */
    String caseIds;
    /**
     * 执行结果
     */
    String executeResult;

    /**
     * 0-未结束 1-结束
     */
    int executeStatus;
}
