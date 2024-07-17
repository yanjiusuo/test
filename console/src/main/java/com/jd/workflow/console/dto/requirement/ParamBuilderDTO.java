package com.jd.workflow.console.dto.requirement;

import com.jd.workflow.console.entity.param.ParamBuilder;
import lombok.Data;

/**
 * @description:
 * @author: sunchao81
 * @Date: 2024-05-11
 */
@Data
public class ParamBuilderDTO extends ParamBuilder {

    /**
     * 总数
     */
    private int total;
}
