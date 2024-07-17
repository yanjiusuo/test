package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 查询appResult
 */
@Data
public class QueryAppResultDTO {

    /**
     * 总条数
     */
    private Long totalCnt = 0l;


    private List<AppInfoDTO> list = new ArrayList<AppInfoDTO>();


    private Integer currentPage;


    private Integer pageSize;
}
