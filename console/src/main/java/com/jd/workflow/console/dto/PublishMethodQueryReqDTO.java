package com.jd.workflow.console.dto;

import lombok.Data;

/**
 * 项目名称：parent
 * 类 名 称：PublishMethodQueryReqDTO
 * 类 描 述：TODO
 * 创建时间：2022-12-28 14:13
 * 创 建 人：wangxiaofei8
 */
@Data
public class PublishMethodQueryReqDTO {

    private Long clusterId;

    private String methodName;

    private String interfaceName;

    private Long currentPage = 1l;

    private Long pageSize = 10l;
}
