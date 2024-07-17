package com.jd.workflow.console.dto.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/29
 */

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/29 
 */
@Data
public class InterfaceSpaceLogParam extends PageParam {

    /**
     * 空间id
     */
    private Long spaceId;
}
