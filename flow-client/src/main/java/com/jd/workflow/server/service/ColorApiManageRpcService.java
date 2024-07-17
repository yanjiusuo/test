package com.jd.workflow.server.service;

import com.jd.workflow.server.dto.QueryResult;
import com.jd.workflow.server.dto.color.ColorApiParamDto;

import java.util.List;

/**
 * 接口管理rpc接口
 * @Api
 */
public interface ColorApiManageRpcService {


    /**
     * 更新网关参数
     * @param params
     * @return
     */
    QueryResult<Integer> updateGateWayParam( List<ColorApiParamDto> params,String methodId);
}
