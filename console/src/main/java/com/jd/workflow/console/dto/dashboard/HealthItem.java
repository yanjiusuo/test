package com.jd.workflow.console.dto.dashboard;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/12
 */

import lombok.Data;

import java.math.BigDecimal;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/12
 */
@Data
public class HealthItem {

    private String name;

    private BigDecimal value;
}
