package com.jd.workflow.console.service.ratelimiting;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.RateLimitingRulesOperateLogMapper;
import com.jd.workflow.console.dto.ratelimiting.RateLimitingQueryDTO;
import com.jd.workflow.console.entity.AppInfoMembers;
import com.jd.workflow.console.entity.RateLimitingRulesOperateLog;
import com.jd.workflow.console.service.IAppInfoMembersService;
import com.jd.workflow.console.service.RateLimitingRulesOperateLogService;
import com.jd.workflow.soap.common.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hanxuefeng13@jd.com
 * @since 2024-02-01
 */
@Service
public class RateLimitingRulesOperateLogServiceImpl extends ServiceImpl<RateLimitingRulesOperateLogMapper, RateLimitingRulesOperateLog> implements RateLimitingRulesOperateLogService {

    @Autowired
    IAppInfoMembersService appInfoMembersService;

    @Override
    public Page<RateLimitingRulesOperateLog> loglist(RateLimitingQueryDTO queryDto, String erp) {
        if (StringUtils.isBlank(queryDto.getAppProvider())){
            BizException bizException = new BizException("appCode不能为空");
            bizException.setFormatPrams(false);
            throw bizException;
        }
        checkUserPermission(queryDto.getAppProvider(), erp);
        LambdaQueryWrapper<RateLimitingRulesOperateLog> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(RateLimitingRulesOperateLog::getAppProvider, queryDto.getAppProvider());
        lambdaQueryWrapper.orderByDesc(RateLimitingRulesOperateLog::getCreateTime);
        Page<RateLimitingRulesOperateLog> page = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        return page(page, lambdaQueryWrapper);
    }

    private void checkUserPermission(String appProvider, String erp) {
        //获取有权限的应用code列表
        LambdaQueryWrapper<AppInfoMembers> appInfoMembersLambdaQueryWrapper = new LambdaQueryWrapper<>();
        appInfoMembersLambdaQueryWrapper.eq(AppInfoMembers::getErp, erp);
        List<AppInfoMembers> list = appInfoMembersService.list(appInfoMembersLambdaQueryWrapper);
        List<String> authedAppCodeList = list.stream().map(AppInfoMembers::getAppCode).distinct().collect(Collectors.toList());

        //根据appCode过滤出应用规则
        if (!authedAppCodeList.contains(appProvider)){
            throw newBizException("没有查询项目"+appProvider+"操作记录的权限");
        }
    }

    private BizException newBizException(String msg){
        BizException bizException = new BizException(msg);
        bizException.setFormatPrams(false);
        return bizException;
    }
}
