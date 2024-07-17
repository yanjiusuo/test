package com.jd.workflow.console.dto;

import lombok.Data;

import java.util.Date;

/**
 * 日志查询请求
 */
@Data
public class CamelLogQueryDTO {


    /**
     * 查询方法列表时传入的接口Id 非空
     */
    private Long interfaceId;

    /**
     * 是佛已发布：1-已发布 0-未发布，默认为1
     */
    private Integer published;
    /**
     * 名称，用来模糊搜索
     */
    private String name;

}
