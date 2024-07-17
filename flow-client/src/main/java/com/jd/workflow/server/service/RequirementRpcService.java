package com.jd.workflow.server.service;

import com.jd.workflow.server.dto.QueryResult;
import com.jd.workflow.server.dto.requirement.QueryRequirementCodeParam;

import java.util.List;

/**
 * @description: 需求相关RPC
 * @author: zhaojingchun
 * @Date: 2024/5/30
 */
public interface RequirementRpcService {


    /**
     * 通过coding地址查和分支名询需求code
     * @param queryParam
     * @return
     */
    QueryResult<List<String>> queryRequirementCodes(QueryRequirementCodeParam queryParam);

}
