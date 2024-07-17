package com.jd.workflow.console.service.jacoco;

import com.jd.workflow.console.base.CommonResult;
import com.jd.workflow.console.entity.jacoco.JacocoResult;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/22
 */
public interface JacocoService {
    /**
     * jacoco服务是否启动
     *
     * @param ip
     * @return
     */
    boolean isJacocoEnabled(String ip);

    /**
     * 开始执行代码覆盖率
     *
     * @param ip
     * @param id
     * @return
     */
    boolean exportJacoco(String ip, Long id);

    /**
     * 查询执行进度，返回结果
     *
     * @param ip
     * @param id
     * @return
     */
    CommonResult<JacocoResult> queryJacocoStage(String ip, Long id);
}
