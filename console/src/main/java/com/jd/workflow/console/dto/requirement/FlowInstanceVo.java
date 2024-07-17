package com.jd.workflow.console.dto.requirement;

import lombok.Data;

@Data
public class FlowInstanceVo {
    Long id;
    private Long flowId;
    private String name;

    private String templateCode;
    private String code;
    /**
     * 状态：1-处理中 2-已完成
     */
    private Integer status;
}
