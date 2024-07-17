package com.jd.workflow.console.entity.debug;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.jd.workflow.console.entity.BaseEntityNoDelLogic;
import lombok.Data;

/**
 * 步骤调试日志
 */
@Data
public class StepDebugLog extends BaseEntityNoDelLogic {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 关联流程的id记录
     */
    private Long flowId;

    private Integer success;

    /**
     * 步骤id
     */
    private String stepId;
    /**
     * 编排方法id
     */
    private String methodId;
    private String logContent;
}
