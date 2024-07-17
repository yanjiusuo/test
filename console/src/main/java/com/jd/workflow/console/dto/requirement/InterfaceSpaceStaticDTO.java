package com.jd.workflow.console.dto.requirement;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/29
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/29
 */
@Data
public class InterfaceSpaceStaticDTO extends InterfaceSpaceDetailDTO {

    /**
     * http接口数
     */
    private Integer httpCount;
    /**
     * http分组数
     */
    private Integer httpGroupCount;
    /**
     * jsf接口数
     */
    private Integer jsfCount;
    /**
     * jsf方法数
     */
    private Integer methodCount;
    /**
     * 成员人数
     */
    private Integer userCount;
    /**
     * 环境数
     */
    private Integer envCount;

    /**
     * 所有接口总数
     */
    private Integer totalMethodCount;

    /**
     * 最近一条空间更新日志
     */
    private String lastLog;
}
