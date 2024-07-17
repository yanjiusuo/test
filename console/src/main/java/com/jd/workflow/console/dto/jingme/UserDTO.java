package com.jd.workflow.console.dto.jingme;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/27
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2024/2/27 
 */
@Data
public class UserDTO {
    /**
     * erp 中文名
     */
    private String realName;
    /**
     * erp
     */
    private String erp;
    /**
     * 默认给个ee
     */
    private String tenantCode;
}
