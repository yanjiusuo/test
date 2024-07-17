package com.jd.workflow.console.dto.test.deeptest.step;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26
 */

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26 
 */
@Data
public class CompareRuleInfo {
    /**
     * 键值对比规则
     */
    private List<CompareRuleKV> presets;
    /**
     * 自定义对比脚本
     */
    private List compareScript = new ArrayList();
    /**
     * 忽略规则
     */
    private String ignorePaths;
    /**
     * 忽略顺序
     */
    private Integer ignoreOrder;
    /**
     * 忽略空值
     */
    private Integer ignoreNull;
}
