package com.jd.workflow.console.dto;

import lombok.Data;

/**
 * 项目名称：parent
 * 类 名 称：QueryClusterReqDTO
 * 类 描 述：TODO
 * 创建时间：2022-12-27 17:55
 * 创 建 人：wangxiaofei8
 */
@Data
public class QueryClusterReqDTO {

    private Long id;

    /**
     * 集群code
     */
    private String clusterCode;

    /**
     * 集群名称
     */
    private String clusterName;

    /**
     * 用户查询
     */
    private String pin;

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
