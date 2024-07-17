package com.jd.workflow.console.dto.flow.param;

import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 20:05
 * @Description: 查询参数分组DTO
 */
@Data
public class QueryParamGroupReqDTO {
    /**
     * 分组id
     */
    private Long id;

    /**
     * 分组名称
     */
    private String groupName;


    /**
     * 当前页数 从1开始
     */
    private Integer currentPage;

    /**
     * 每页条数 最多100条
     */
    private Integer pageSize;


    public void initPageParam(Integer maxPageSize) {
        if (this.currentPage == null || this.currentPage < 1) {
            this.currentPage = 1;
        }
        if (this.pageSize == null || this.pageSize < 1 || this.pageSize > maxPageSize) {
            this.pageSize = maxPageSize;
        }
    }
}
