package com.jd.workflow.console.rpc;

import com.jd.fastjson.JSON;
import com.jd.workflow.console.dto.MethodManageDTO;
import com.jd.workflow.console.service.usecase.CaseSetExeLogService;
import com.jd.workflow.server.dto.JsfOrHttpMethodInfo;
import com.jd.workflow.server.dto.QueryResult;
import com.jd.workflow.server.dto.requirement.QueryRequirementCodeParam;
import com.jd.workflow.server.service.RequirementRpcService;
import com.jd.workflow.soap.common.exception.BizException;
import com.jd.workflow.soap.common.lang.Guard;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @description:
 * @author: zhaojingchun
 * @Date: 2024/5/30
 */
@Service
@Slf4j
public class RequirementRpcServiceImpl implements RequirementRpcService {

    @Autowired
    private CaseSetExeLogService caseSetExeLogService;

    @Override
    public QueryResult<List<String>> queryRequirementCodes(QueryRequirementCodeParam queryParam) {
        List<String> requirementCodes = null;
        try {
            requirementCodes = caseSetExeLogService.queryRequirementCodes(queryParam);
        } catch (BizException e) {
            log.error("RequirementRpcServiceImpl.queryRequirementCodes queryParam {} Exception ", JSON.toJSONString(queryParam), e);
            return QueryResult.error("查询方法失败" + e.getMsg());
        } catch (Throwable e) {
            log.error("RequirementRpcServiceImpl.queryRequirementCodes queryParam {} Throwable", JSON.toJSONString(queryParam), e);
            return QueryResult.error("查询方法失败请联系接口负责人");
        }
        return QueryResult.buildSuccessResult(requirementCodes);
    }
}
