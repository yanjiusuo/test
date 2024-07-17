package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.Date;

/**
 * 日志查询请求
 */
@Data
public class CamelLogReqDTO {

    /**
     * 具体日志的id
     */
    private Long id;

    /**
     * 查询日志时，指定具体方法
     */
    private Long methodId;

    /**
     * 查询方法列表时传入的接口Id 非空
     */
    private Long interfaceId;

    /**
     * 查询接口列表的名称
     */
    private String name;

    /**
     *  0 正常  1 异常
     */
    private Integer logLevel;

    /**
     * 发布版本
     */
    private String version;

    /**
     * 调用日志查询时时间开始
     */
    private Date startDate;

    /**
     * 调用日志查询时时间结束
     */
    private Date endDate;

    /**
     *当前页数 从1开始
     */
    private Integer currentPage;

    /**
     * 每页条数 日志查询最多10条 接口或者方法最多20条
     */
    private Integer pageSize;


}
