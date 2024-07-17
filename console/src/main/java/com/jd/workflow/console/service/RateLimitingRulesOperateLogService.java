package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.ratelimiting.RateLimitingQueryDTO;
import com.jd.workflow.console.entity.RateLimitingRulesOperateLog;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author hanxuefeng13@jd.com
 * @since 2024-02-01
 */
public interface RateLimitingRulesOperateLogService extends IService<RateLimitingRulesOperateLog> {

    Page<RateLimitingRulesOperateLog> loglist(RateLimitingQueryDTO queryDto, String erp);
}
