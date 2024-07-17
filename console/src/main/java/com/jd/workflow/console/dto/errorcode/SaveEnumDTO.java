package com.jd.workflow.console.dto.errorcode;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/7
 */
@Data
public class SaveEnumDTO {
    /**
     * 应用id
     */
    private Long appId;


    /**
     * 枚举id,错误码保存传空
     */
    private Long enumId;
    /**
     * 具体错误码
     */
    private List<EnumPropDTO> enumPropDTOList;
}
