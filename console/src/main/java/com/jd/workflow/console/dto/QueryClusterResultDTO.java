package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.List;

/**
 * 项目名称：parent
 * 类 名 称：QueryClusterResultDTO
 * 类 描 述：TODO
 * 创建时间：2022-12-27 17:58
 * 创 建 人：wangxiaofei8
 */
@Data
public class QueryClusterResultDTO {

    /**
     * 总条数
     */
    private Long totalCnt = 0l;


    private List<PublishClusterDTO> list;


    private Integer currentPage;

    /**
     * 每页条数
     */
    private Integer pageSize;
}
