package com.jd.workflow.console.dto;

import com.jd.workflow.console.base.PageParam;
import lombok.Data;

@Data
public class ApprovalPageQuery extends PageParam {

    /**
     * 接口名称
     */
    private String name;

    private String contributor;

    private Integer status;


}
