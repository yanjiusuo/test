package com.jd.workflow.console.dto;

import lombok.Data;

/**
 * 查询App
 */
@Data
public class QueryAppReqDTO {

    private Long id;

    /**
     * 应用code
     */
    private String appCode;

    private String appType;

    /**s
     * 应用名称
     */
    private String appName;
    /**
     * 应用名称或者应用编码
     */
    private String name;

    /**
     * 调用级别
     */
    private String authLevel;

    /**
     * 用户查询
     */
    private String pin;

    /**
     * 租户id
     */
    private String tenantId;

    /**
     * 业务域信息
     */
    private String cjgBusinessDomainTrace;
    /**
     * 产品信息
     */
    private String  cjgProductTrace;
    /**
     * 部门明称 中文
     */
    private String dept;

    private String admin;

    /**
     *当前页数 从1开始
     */
    private Integer currentPage;

    /**
     * 每页条数 最多100条
     */
    private Integer pageSize;


    public void initPageParam(Integer maxPageSize){
        if(this.currentPage==null||this.currentPage<1){
            this.currentPage = 1;
        }
        if(this.pageSize==null||this.pageSize<1||this.pageSize>maxPageSize){
            this.pageSize = maxPageSize;
        }
    }
}
