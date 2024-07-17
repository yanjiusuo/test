package com.jd.workflow.console.rpc;

import java.time.LocalDateTime;

import com.jd.workflow.console.entity.logic.BizLogicInfo;
import com.jd.workflow.console.service.logic.IBizLogicInfoService;
import com.jd.workflow.server.dto.BizLogicInfoDto;
import com.jd.workflow.server.dto.QueryResult;
import com.jd.workflow.server.service.BizLogicInfoRpcService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/6/18
 */
@Service
@Slf4j
public class BizLogicInfoRpcServiceImpl implements BizLogicInfoRpcService {

    @Autowired
    private IBizLogicInfoService bizLogicInfoService;

    @Override
    public QueryResult<Boolean> syncBizLogicInfo(BizLogicInfoDto bizLogicInfoDto) {
        QueryResult queryResult = null;
        BizLogicInfo bizLogicInfo = convert(bizLogicInfoDto);
        boolean resultBool = Boolean.FALSE;
        try {
            resultBool = bizLogicInfoService.saveOrUpdateData(bizLogicInfo);
            queryResult = QueryResult.buildSuccessResult(resultBool);
        } catch (Exception e) {
            queryResult = QueryResult.buildErrorCodeMsg(5001, e.getMessage());
            log.error("BizLogicInfoRpcServiceImpl.syncBizLogicInfo Exception", e);
        }
        return queryResult;
    }

    /**
     * 数据转换
     *
     * @param bizLogicInfoDto
     * @return
     */
    private BizLogicInfo convert(BizLogicInfoDto bizLogicInfoDto) {
        BizLogicInfo bizLogicInfo = new BizLogicInfo();
        if (bizLogicInfo == null) {
            bizLogicInfo = null;
        }
        bizLogicInfo.setInterfaceName(bizLogicInfoDto.getInterfaceName());
        bizLogicInfo.setMethodName(bizLogicInfoDto.getMethodName());
        bizLogicInfo.setComponentId(bizLogicInfoDto.getComponentId());
        bizLogicInfo.setType(bizLogicInfoDto.getType());
        return bizLogicInfo;
    }
}
