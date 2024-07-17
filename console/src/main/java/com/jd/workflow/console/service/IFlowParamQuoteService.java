package com.jd.workflow.console.service;

import com.jd.workflow.console.dto.flow.param.*;

import java.util.List;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/21 17:05
 * @Description: 关联公共参数service
 */
public interface IFlowParamQuoteService {

    /**
     * 关联公共参数
     *
     * @param flowParamQuoteDTO
     * @return
     */
    Boolean quoteParam(FlowParamQuoteDTO flowParamQuoteDTO);

    /**
     * 取消关联公共参数
     *
     * @param flowParamQuoteDTO
     * @return
     */
    Boolean cancelQuoteParam(FlowParamQuoteDTO flowParamQuoteDTO);

    /**
     * 查询已关联公共参数
     *
     * @param queryDTO
     * @return
     */
    QueryParamQuoteResultDTO queryQuoteParam(QueryParamQuoteReqDTO queryDTO);

    /**
     * 查询未关联公共参数
     *
     * @param queryDTO
     * @return
     */
    QueryParamResultDTO queryUnQuoteParam(QueryParamQuoteReqDTO queryDTO);

    /**
     * 查询已关联公共参数 并按照参数所属分组归堆
     *
     * @param queryDTO
     * @return
     */
    QueryParamQuoteForGroupResultDTO queryQuoteParamForGroup(QueryParamQuoteReqDTO queryDTO);
}
