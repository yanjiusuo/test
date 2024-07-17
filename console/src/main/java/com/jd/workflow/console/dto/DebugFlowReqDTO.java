package com.jd.workflow.console.dto;

import lombok.Data;

/**
 * debug调试日志列表查询入参
 */
@Data
public class DebugFlowReqDTO {

    /**
     * 流程方法ID
     */
    private Long methodId;

    private String erp;

    /**
     * 执行状态 1 成功 0 失败
     */
    private Integer success;

    /**
     * 描述信息
     */
    private String desc;

    /**
     *当前页数 从1开始
     */
    private Integer currentPage;

    /**
     * 每页条数
     */
    private Integer pageSize;

    /**
     * 页面标签 1=color
     */
    private Integer tag;

}
