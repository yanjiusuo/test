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
public class AddSpaceUserDTO {
    /**
     * 空间id
     */
    private Long spaceId;

    /**
     * 批量添加成员erp
     */
    private List<String> userErpList;

    /**
     *  负责人erp
     */
    private String owner;
}
