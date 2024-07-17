package com.jd.workflow.console.dto.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/28
 */

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/28 
 */
@Data
public class RequirementInfoLogDTO {

    private Long requirementId;
    private String erp;
    private Date created;
    private Long id;

}
