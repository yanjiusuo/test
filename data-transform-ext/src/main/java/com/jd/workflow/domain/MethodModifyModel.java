package com.jd.workflow.domain;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */

import com.jd.matrix.sdk.base.DomainModel;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/6
 */
@Data
@NoArgsConstructor
public class MethodModifyModel implements DomainModel {
    /**
     * 方法信息
     */
    private MethodInfo methodInfo;
    /**
     * 接口信息
     */
    private InterfaceInfo interfaceInfo;
    /**
     * 执行人
     */
    private String erp;
}
