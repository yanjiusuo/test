package com.jd.workflow.console.dto.requirement;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-28
 */
@Data
@AllArgsConstructor
public class AssertionStatisticsDTO {

    /**
     *
     */
    private boolean res;

    /**
     *
     */
    private Long totalCount;

    /**
     *
     */
    private Long failCount;


    /**
     *
     */
    private Long successCount;
}
