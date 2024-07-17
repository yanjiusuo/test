package com.jd.workflow.console.dto.flow.param;

import com.jd.workflow.console.entity.FlowParamGroup;
import lombok.Data;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 20:18
 * @Description: 查询参数分组result
 */
@Data
public class QueryParamGroupResultDTO {

    /**
     * 总条数
     */
    private Long totalCnt = 0l;


    private List<FlowParamGroup> list;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 每页展示条数
     */
    private Integer pageSize;
}
