package com.jd.workflow.console.dto.share;

import com.jd.workflow.console.entity.share.InterfaceShareGroup;
import lombok.Data;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/4/3 17:09
 * @Description:
 */
@Data
public class QueryShareGroupResultDTO {
    /**
     * 总条数
     */
    private Long totalCnt = 0l;


    private List<InterfaceShareGroup> list;

    /**
     * 当前页
     */
    private Integer currentPage;

    /**
     * 每页展示条数
     */
    private Integer pageSize;
}
