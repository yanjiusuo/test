package com.jd.workflow.console.dto.flow.param;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/21 10:57
 * @Description: 查询参数列表DTO
 */
@Data
public class QueryParamReqDTO {
    /**
     * 参数名称
     */
    private String paramName;

    /**
     * 参数所属分组id
     */
    private Long groupId;

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

    public Integer getOffset() {
        return (currentPage - 1) * pageSize;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }


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
