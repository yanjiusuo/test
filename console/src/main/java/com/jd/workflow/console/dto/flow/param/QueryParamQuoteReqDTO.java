package com.jd.workflow.console.dto.flow.param;

import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/21 21:53
 * @Description:
 */
@Data
public class QueryParamQuoteReqDTO {

    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数所属分组id
     */
    private Long groupId;

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
