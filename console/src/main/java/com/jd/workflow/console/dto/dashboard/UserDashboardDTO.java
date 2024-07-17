package com.jd.workflow.console.dto.dashboard;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/11
 */
@Data
public class UserDashboardDTO {

    /**
     * 接口总数
     */
    private Integer totalInterfaceCount;

    private Integer changeInterfaceCount;

    /**
     * http接口总数
     */
    private Integer totalHttpCount;
    /**
     * http接口变化数
     */
    private Integer changeHttpCount;

    /**
     * jsf 接口总数
     */
    private Integer totalJsfCount;
    /**
     * jsf 接口变化数
     */
    private Integer changeJsfCount;

    /**
     * jsf方法总数
     */
    private Integer totalJsfMethodCount;
    /**
     * jsf方法变化数
     */
    private Integer changeJsfMethodCount;

    /**
     * 有权限应用总数
     */
    private Integer totalAppCount;
    /**
     * 有权限应用变化数
     */
    private Integer changeAppCount;


    /**
     * http接口鉴权数
     */
    private Integer httpAuthCount;

    /**
     * jsf接口鉴权数
     */
    private Integer jsfAuthCount;

    /**
     * 内置标签数
     */
    private Integer innerTagCount;

    /**
     *
     * 用户自定义标签数
     */
    private Integer userTagCount;


    /**
     * 接口空间数
     */
    private Integer spaceCount;

    /**
     * 支持需求数
     */
    private Integer requireCount;

    /**
     * 健康度大于60分接口数
     */
    private Integer score60Count;

    /**
     * 接口文档访问数
     */
    private Integer interfaceViewCount;
}
