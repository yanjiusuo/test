package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author hanxuefeng13@jd.com
 * @since 2024-03-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bdp_rate_limiting_rules_config")
public class RateLimitingRulesConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 服务提供方
     */
    private String appProvider;

    /**
     * 0关1开
     */
    private Integer globalSwitch;

    /**
     * 0关1开
     */
    private Integer appidAllowEmpty;

    /**
     * 全局限流值
     */
    private Integer globalRateLimitingValue;

    /**
     * 权限限流值开关-给页面用
     */
    private Integer globalRateLimitingValueSwitch;


}
