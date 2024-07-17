package com.jd.workflow.console.dto;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/29
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/11/29 
 */
@Data
public class FindMethodInterfaceParam {

    /**
     * 1接口，2 文件夹，3分组
     */
    private Integer type;
    /**
     * 方法，分组，接口id
     */
    private Long methodId;
}
