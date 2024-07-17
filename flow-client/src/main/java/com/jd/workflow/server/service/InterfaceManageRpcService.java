package com.jd.workflow.server.service;

import com.jd.workflow.server.dto.QueryResult;
import com.jd.workflow.server.dto.interfaceManage.JsfBatchInterface;

import java.util.List;

/**
 * 接口管理rpc接口
 * @Api
 */
public interface InterfaceManageRpcService {


    /**
     * 增加方法批量
     * @param groups
     * @return
     */
    public QueryResult<List<JsfBatchInterface>> addMethodBatch( Long appId, List<JsfBatchInterface> groups);








}
