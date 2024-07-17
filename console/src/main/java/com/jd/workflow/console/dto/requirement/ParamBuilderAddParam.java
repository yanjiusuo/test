package com.jd.workflow.console.dto.requirement;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

/**
 * @description:
 * @author: yza
 * @Date: 2024-05-23
 */
@Data
public class ParamBuilderAddParam {

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
