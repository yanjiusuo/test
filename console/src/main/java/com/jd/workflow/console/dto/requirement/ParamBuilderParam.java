package com.jd.workflow.console.dto.requirement;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-11
 */
@Data
public class ParamBuilderParam extends PageParam {

    /**
     * 模糊查询
     */
    private String sceneName;

    /**
     * 方法id
     */
    private Long methodManageId;

    /**
     * 入参json
     */
    private String paramJson;

    /**
     * 主键
     */
    private Long id;
}
