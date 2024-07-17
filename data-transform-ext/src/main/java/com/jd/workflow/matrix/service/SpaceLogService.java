package com.jd.workflow.matrix.service;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6 
 */
public interface SpaceLogService {

    /**
     * 添加日志
     * @param interfaceId
     * @param erp
     * @param doSomething
     */
    void createSpaceMethodLog(Long interfaceId,String erp,String doSomething);
}
