package com.jd.workflow.console.listener.cjg.entity;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
@Data
public class WFFlowInstanceCreateMessageBody implements IFlowMessage{

    /**
     * flow的id
     */
    private Long flowId;

    /**
     * 需求名称
     */
    private String name;

    /**
     * 需求code
     */
    private String code;

    /**
     * 该流程包含的节点组件类型code的集合
     */
    private Set<String> nodeTypeCodeSet;

    /**
     * 创建时间
     */
    private LocalDateTime created;

    /**
     * source，在线联调的值为1
     */
    private Integer source;
}