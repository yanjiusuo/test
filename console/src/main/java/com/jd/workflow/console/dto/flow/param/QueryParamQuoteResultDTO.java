package com.jd.workflow.console.dto.flow.param;

import com.jd.workflow.console.entity.FlowParamQuote;
import lombok.Data;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/21 21:56
 * @Description:
 */
@Data
public class QueryParamQuoteResultDTO {
    /**
     * 总条数
     */
    private Long totalCnt = 0l;


    private List<FlowParamQuote> list;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 每页展示条数
     */
    private Integer pageSize;
}
