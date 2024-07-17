package com.jd.workflow.console.dto.flow.param;

import com.jd.workflow.console.entity.FlowParam;
import com.jd.workflow.console.entity.FlowParamGroup;
import lombok.Data;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/21 11:00
 * @Description: 查询参数列表出参DTO
 */
@Data
public class QueryParamResultDTO {
    /**
     * 总条数
     */
    private Long totalCnt = 0l;


    private List<FlowParam> list;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 每页展示条数
     */
    private Integer pageSize;
}
