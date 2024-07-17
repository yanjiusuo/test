package com.jd.workflow.console.dto.flow.param;

import java.util.List;

import lombok.Data;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/21 21:04
 * @Description:
 */
@Data
public class FlowParamQuoteDTO {
    /**
     * 接口id
     */
    private Long interfaceId;

    /**
     * 公共参数id集合
     */
    private List<Long> paramIds;
}
