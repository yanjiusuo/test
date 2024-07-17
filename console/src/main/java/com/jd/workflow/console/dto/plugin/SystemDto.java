package com.jd.workflow.console.dto.plugin;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/6
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/6
 */
@Data
public class SystemDto {
    String systemName;
    String systemCode;
    String tenant;
    String erp;
    String env;
}
