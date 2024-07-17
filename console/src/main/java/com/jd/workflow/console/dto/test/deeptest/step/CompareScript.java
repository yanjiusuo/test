package com.jd.workflow.console.dto.test.deeptest.step;/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26
 */

import lombok.Data;

/**
 * @description:
 * @author: chenyufeng18
 * @Date: 2023/9/26 
 */
@Data
public class CompareScript {
    /**
     * 脚本内容
     */
    private String customScript;
    /**
     * 脚本类型
     */
    private String scriptType;

}
