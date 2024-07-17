package com.jd.workflow.console.dto.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/16
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/16
 */
@Data
public class DeleteEnumPropDTO {
    /**
     * 应用id
     */
    private Long appId;

    /**
     * 枚举id
     */
    private Long enumId;

    /**
     * 枚举值或错误码值id
     */
    private List<Long> enumPropIds;
}
