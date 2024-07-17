package com.jd.workflow.console.dto.dashboard;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */

import lombok.Data;

import java.util.Date;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */
@Data
public class InterfaceArrangeDTO {
    /**
     * 接口名称
     */
    private String name;
    /**
     * 接口编码
     */
    private String code;
    /**
     * 创建时间
     */
    private Date time;
}
