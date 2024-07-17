package com.jd.workflow.console.dto.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/29
 */

import lombok.Data;

import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/29 
 */
@Data
public class RemoveSpaceUserDTO {

    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 批量移除成员erp
     */
    private List<String> userErpList;

}
