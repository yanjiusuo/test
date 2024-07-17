package com.jd.workflow.console.dto;

import lombok.Data;

@Data
public class FilterParam {

    /**
     * 来源 2 客户端必传信息
     */
    private Integer source;

    /**
     * 是否必填 1=是
     */
    private Integer isNecessary;

}
