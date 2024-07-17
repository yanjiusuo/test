package com.jd.workflow.console.listener.cjg.entity;

import lombok.Data;


/**
 * 流程实例删除|人员变更|结束  共用一类数据结构
 */
@Data
public class WFlowChangeMessageBody implements IFlowMessage {

    /**
     * flow的id
     */
    private Long flowId;

    /**
     * 触发时间
     */
    private String processTime;

    /**
     * source，在线联调的值为1
     */
    private Integer source;
}