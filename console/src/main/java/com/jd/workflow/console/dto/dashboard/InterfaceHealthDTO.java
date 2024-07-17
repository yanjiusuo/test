package com.jd.workflow.console.dto.dashboard;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */
@Data
public class InterfaceHealthDTO {

    /**
     * 平均健康度
     */
    private BigDecimal avgCount;




    private List<HealthItem> healthItemList;
}
