package com.jd.workflow.console.dto.test.deeptest;

import lombok.Data;

@Data
public class BaseTestEntityInfo {
    /**
     * 目录创建人
     */
    String owner;
    /**
     * 修改人
     */
    String editor;
    /**
     * 创建时间
     */
    String createTime;
    /**
     * 修改时间
     */
    String updateTime;
    String source ="cjg";

}
