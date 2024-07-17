package com.jd.workflow.matrix.ext.spi;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/1
 */

import com.jd.matrix.generic.annotation.GenericExtension;
import com.jd.matrix.generic.annotation.GenericSPI;
import com.jd.workflow.common.ObjectJsonType;
import com.jd.workflow.method.MethodInfo;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/8/1
 */
@GenericSPI(domainCode = "houduanjishuyu.PaaShuakaifangyu")
public interface HealthCheckSPI {

    /**
     * 解析字符串
     * @param content 内容
     * @return
     */
    @GenericExtension(code ="HEALTH_CHECK" , name = "健康检查")
    Boolean isTypeValid(MethodInfo content);

    String getType();
}
