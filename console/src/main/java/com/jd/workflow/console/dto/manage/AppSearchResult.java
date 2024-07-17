package com.jd.workflow.console.dto.manage;

import lombok.Data;

/**
 * 应用搜索结果
 */
@Data
public class AppSearchResult {
    /**
     * 应用id
     */
    Long id;
    /**
     * 应用编码
     */
    String appCode;
    /**
     * 应用名称
     */
    String appName;

    String jdosAppCode;
}
