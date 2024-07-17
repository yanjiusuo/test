package com.jd.workflow.console.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.jd.workflow.console.dto.ratelimiting.RateLimitingChangeStatusDTO;
import com.jd.workflow.console.dto.ratelimiting.RateLimitingQueryDTO;
import com.jd.workflow.console.entity.RateLimitingRules;
import com.jd.workflow.console.entity.RateLimitingRulesConfig;

import java.util.List;

/**
 * <p>
 * 服务总线限流规则表 服务类
 * </p>
 *
 * @author hanxuefeng13@jd.com
 * @since 2024-01-23
 */
public interface RateLimitingRulesService extends IService<RateLimitingRules> {

    Page<RateLimitingRules> listRules(RateLimitingQueryDTO erp);

    List<Long> addRules(List<RateLimitingRules> list, String erp);

    void updateRules(List<RateLimitingRules> list, String erp);

    void deleteRules(List<Long> ids, String erp);

    void changeStatus(RateLimitingChangeStatusDTO list, String erp);

    void globalSettings(RateLimitingRulesConfig config, String erp);

    RateLimitingRulesConfig getGlobalSettings(String appProvider, String erp);
}
