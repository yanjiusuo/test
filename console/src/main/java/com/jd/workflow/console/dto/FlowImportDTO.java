package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.Map;

/**
 *
 * @date: 2022/6/1 17:36
 * @author wubaizhao1
 */
@Data
public class FlowImportDTO {
    /**
     * 方法id
     */
    private Long id;
    /**
     * 编排的
     */
    private Map<String,Object> definition;
}
