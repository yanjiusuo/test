package com.jd.workflow.server.service;

import com.jd.workflow.server.dto.BizLogicInfoDto;
import com.jd.workflow.server.dto.QueryResult;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/6/17
 */
public interface BizLogicInfoRpcService {

    /**
     * 同步BizLogicInfo信息
     *
     * @param bizLogicInfoDto
     * @return
     */
    QueryResult<Boolean> syncBizLogicInfo(BizLogicInfoDto bizLogicInfoDto);

}
