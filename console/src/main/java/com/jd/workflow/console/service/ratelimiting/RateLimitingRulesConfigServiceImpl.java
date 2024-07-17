package com.jd.workflow.console.service.ratelimiting;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jd.workflow.console.dao.mapper.RateLimitingRulesConfigMapper;
import com.jd.workflow.console.entity.RateLimitingRulesConfig;
import com.jd.workflow.console.service.RateLimitingRulesConfigService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author hanxuefeng13@jd.com
 * @since 2024-03-11
 */
@Service
public class RateLimitingRulesConfigServiceImpl extends ServiceImpl<RateLimitingRulesConfigMapper, RateLimitingRulesConfig> implements RateLimitingRulesConfigService {

}
