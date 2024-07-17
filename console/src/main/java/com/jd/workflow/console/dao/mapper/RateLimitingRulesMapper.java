package com.jd.workflow.console.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jd.workflow.console.entity.RateLimitingRules;
import org.springframework.stereotype.Repository;

/**
 * <p>
 * 服务总线限流规则表 Mapper 接口
 * </p>
 *
 * @author hanxuefeng13@jd.com
 * @since 2024-01-23
 */
@Repository
public interface RateLimitingRulesMapper extends BaseMapper<RateLimitingRules> {

}
