package com.jd.workflow.console.dto.share;

import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/3 17:05
 * @Description:
 */
@Data
public class QueryShareGroupReqDTO {

    /**
     * 分组名称
     */
    private String shareGroupName;


    /**
     * 0：我分享的  1：分享给我的
     */
    private int type;

    /**
     * 创建人code
     */
    private String creator;

    /**
     * 当前页数 从1开始
     */
    private Integer currentPage;

    /**
     * 每页条数 最多100条
     */
    private Integer pageSize;


    /**
     * 当前页号
     */
    private Integer offset;


    public void initPageParam(Integer maxPageSize) {
        if (this.currentPage == null || this.currentPage < 1) {
            this.currentPage = 1;
        }
        if (this.pageSize == null || this.pageSize < 1 || this.pageSize > maxPageSize) {
            this.pageSize = maxPageSize;
        }
        this.offset = (currentPage - 1) * pageSize;
    }
}
