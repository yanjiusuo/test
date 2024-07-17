package com.jd.workflow.console.service.role;

import com.alibaba.fastjson.JSON;
import com.jd.cjg.acc.client.entity.param.query.AccPageQuery;
import com.jd.cjg.acc.client.entity.param.query.UserQueryParam;
import com.jd.cjg.acc.client.entity.po.UserTenantInfo;
import com.jd.cjg.acc.client.entity.result.AccPageResult;
import com.jd.cjg.acc.client.service.AccUserService;
import com.jd.cjg.acc.client.utils.AccServiceUtil;
import com.jd.cjg.flow.sdk.model.result.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.function.Supplier;

@Slf4j

@Service
public class AccUserServiceAdapter  {
    @Value("${acc.appId:up-portal}")
    private String appId;

    @Value("${acc.channelId:10}")
    private Long channelId;
    @Value("${acc.tenantId:26001}")
    private Long tenantId;
    @Resource
    private AccUserService accUserService;

    /**
     * 659 japi_admin
     * @param operator
     * @param roleId
     * @return
     */
    public PageResult<UserTenantInfo> getUsersByChannel(String operator, Long roleId) {
        Supplier<AccPageQuery<UserQueryParam>> supplier = () -> {
            AccPageQuery<UserQueryParam> accPageQuery = new AccPageQuery<>();
            UserQueryParam userQueryParam = new UserQueryParam();
            userQueryParam.setTenantId(tenantId);
            userQueryParam.setChannelId(channelId);
            userQueryParam.setRoleId(roleId);
            accPageQuery.setData(userQueryParam);
            accPageQuery.setCurrentPage(1);
            accPageQuery.setPageSize(20);
            return accPageQuery;
        };
        AccPageResult<UserTenantInfo> accPageResult = AccServiceUtil.callServiceCustomize(operator, appId,
                supplier, accUserService::queryAccUserTenantsByPage_pt, result -> {
                    log.error("RPC queryUsers error,errCode={},errMsg={}", result.getErrCode(), result.getErrMsg());
                });

        log.info("getUsersByChannel accPageResult={}", JSON.toJSONString(accPageResult));
        PageResult<UserTenantInfo> pageResult = new PageResult<>();
        pageResult.setPageSize(accPageResult.getPageSize());
        pageResult.setData( accPageResult.getData());
        pageResult.setTotalCount(accPageResult.getTotalCount());
        log.info("getUsersByChannel users={}", JSON.toJSONString(pageResult.getData()));

        return pageResult;
    }
}
