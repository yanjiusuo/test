package com.jd.workflow.console.dto.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/3/6
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/3/6
 */
@Data
public class EnumFullDTO extends EnumDTO {

    /**
     * 枚举值
     */
    private List<EnumPropDTO> enumPropDTOList;
}
