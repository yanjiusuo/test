package com.jd.workflow.console.dto;

import lombok.Data;

/**
 * 类 名 称：QueryHttpAuthReqDTO
 * 类 描 述：查询鉴权标识
 * @date 2023-01-06 11:30
 * @author wangwenguang
 */
@Data
public class QueryHttpAuthApplyReqDTO {

    /**
     * id
     */
    private Long id;

    /**
     * 站点
     */
    private String site;

    /**s
     * 应用信息
     */
    private String appInfo;

    /**s
     * 鉴权信息
     */
    private String authInfo;
    /**
     * 应用code
     */
    private String appCode;

    /**s
     * 应用名称
     */
    private String appName;

    /**
     * 调用方应用编码
     */
    private String callAppCode;

    /**
     * 调用方应用名称
     */
    private String callAppName;

    /**
     * 鉴权标识
     */
    private String authCode;


    /**
     * 申请单ID
     */
    private String ticketId;

    /**
     * 状态，审批状态
     */
    private Integer ticketStatus;

    /**
     * token有值
     */
    private Boolean hasToken;

    /**
     * 用户查询
     */
    private String pin;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     *当前页数 从1开始
     */
    private Long current = 1L;

    /**
     * 每页条数 最多100条
     */
    private Integer pageSize = 10;

    /**
     * 当前页号
     */
    private Long offset;

    public Long getOffset() {
        return (current -1) * pageSize;
    }

    public void setOffset(Long offset) {
        this.offset = offset;
    }

}
