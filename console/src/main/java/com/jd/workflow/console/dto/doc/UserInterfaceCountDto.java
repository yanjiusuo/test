package com.jd.workflow.console.dto.doc;

import lombok.Data;

/**
 *  用户接口数据统计
 */
@Data
public class UserInterfaceCountDto {
    /**
     * 自动上报的接口数量
     */
    Long autoReportCount;
    /**
     * 非自动上报的接口数量
     */
    Long nonAutoReportCount;
}
