package com.jd.workflow.console.service.parser;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/4/26
 */
public interface FilterAttributeService {
    /**
     * 过滤处理的属性
     * @param attributeKey
     * @return
     */
    boolean filter(String attributeKey);
}
