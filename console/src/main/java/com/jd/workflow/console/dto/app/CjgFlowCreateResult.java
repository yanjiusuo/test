package com.jd.workflow.console.dto.app;

import lombok.Data;

@Data
public class CjgFlowCreateResult {
    /**
     * 创建后的流程id
     */
    Long id;
    /**
     * 关联的流程id
     */
    Long flowId;
}
