package com.jd.workflow.console.dto.dept;

import lombok.Data;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/10 14:43
 * @Description:
 */
@Data
public class QueryDeptResultDTO {

    /**
     * 总条数
     */
    private Long totalCnt = 0l;

    /**
     * 部门名称List
     */
    private List<String> list;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 每页条数 最多100条
     */
    private Integer pageSize;

    /**
     * 部门编码全路径
     */
    private String organizationFullPath;

    /**
     * 部门编码
     */
    private String organizationCode;

    /**
     * 部门名称全路径
     */
    private String organizationFullname;

    /**
     * 部门名称
     */
    private String organizationName;

    /**
     * 部门级别
     */
    private String organizationLevel;

    /**
     * 是否含有子部门
     */
    private boolean hasChildDepartment;
}
