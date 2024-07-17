package com.jd.workflow.console.dto.test;

import lombok.Data;

@Data
public class DeepTestBaseDto {
    /**
     * station 站点 test-测试站 zh-中国站
     */
    String station;
    /**
     * 根用例集id,当前用例集最顶层的用例集id
     */
    Long rootSuiteId;
    /**
     * 模块id
     */
    Long moduleId;

    public void init(){
        if("master".equals(station)){
            station = "China";
        }
    }
}
