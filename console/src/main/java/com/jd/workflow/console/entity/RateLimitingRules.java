package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 服务总线限流规则表
 * </p>
 *
 * @author hanxuefeng13@jd.com
 * @since 2024-01-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bdp_rate_limiting_rules")
public class RateLimitingRules implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 提供方应用code
     */
    private String appProvider;

    /**
     * 提供方应用名称
     */
    private String appProviderName;

    /**
     * 接口
     */
    private String interfacePath;

    /**
     * 使用方应用code
     */
    private String appConsumer;

    /**
     * 使用方应用名称
     */
    private String appConsumerName;

    /**
     * 限流阈值
     */
    private Integer threshold;

    /**
     * 状态-0未启用1启用
     */
    private Integer status;

    /**
     * 备注
     */
    private String ruleName;

    /**
     * 规则类型
     */
    private Integer ruleType;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String erp;


}
