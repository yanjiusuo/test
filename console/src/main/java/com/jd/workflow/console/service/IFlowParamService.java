package com.jd.workflow.console.service;

import com.jd.workflow.console.dto.flow.param.*;
import com.jd.workflow.console.entity.FlowParam;

/**
 * @Auther: xinwengang
 * @Date: 2023/3/20 16:11
 * @Description: 公共参数管理service
 */
public interface IFlowParamService {

    /**
     * 新增参数
     *
     * @param dto
     * @return
     */
    Long addParam(FlowParamDTO dto);

    /**
     * 修改分组
     *
     * @param dto
     * @return
     */
    Boolean editParam(FlowParamDTO dto);

    /**
     * 删除分组
     *
     * @param id
     * @return
     */
    Boolean removeParam(Long id);

    /**
     * 查询参数列表
     * @param dto
     * @return
     */
    QueryParamResultDTO queryParams(QueryParamReqDTO dto);

    /**
     * 根据Id查询参数信息
     * @param paramId
     * @return
     */
    FlowParam getParamById(Long paramId);
}
