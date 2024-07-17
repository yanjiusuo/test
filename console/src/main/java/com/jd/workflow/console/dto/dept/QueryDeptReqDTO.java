package com.jd.workflow.console.dto.dept;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/10 14:42
 * @Description: 查询部门名称
 */
@Data
public class QueryDeptReqDTO {

    /**
     * 部门名称
     */
    private String deptName;

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
