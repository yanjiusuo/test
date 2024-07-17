package com.jd.workflow.console.model.sync;

import lombok.Delegate;
import lombok.Getter;
import lombok.Setter;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
@Getter
@Setter
public class InterfaceJsonInfo {

    /**
     * 接口类全名
     */
    private String interfaceClassFullName;

    /**
     * 每叶大小
     */
    int pageSize = 30;
    /**
     *  起始值
     */
    int startNo = 0;

    /**
     * 最大循环次数
     */
    int maxWhileNo = 100;





}
