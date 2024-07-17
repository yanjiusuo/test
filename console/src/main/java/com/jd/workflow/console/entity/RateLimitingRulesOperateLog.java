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
 * 
 * </p>
 *
 * @author hanxuefeng13@jd.com
 * @since 2024-02-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("bdp_rate_limiting_rules_operate_log")
public class RateLimitingRulesOperateLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 操作id，批量操作的情况下会产生一个id
     */
    private Long operateId;

    /**
     * 操作类型
     * 1-创建规则
     * 2-修改规则
     * 3-删除规则
     * 4-上线
     * 5-下线
     */
    private Integer operateType;

    /**
     * 操作人
     */
    private String erp;

    /**
     * 服务提供方
     */
    private String appProvider;

    /**
     * 服务使用方
     */
    private String appConsumer;

    /**
     * 接口名称
     */
    private String interfacePath;

    /**
     * 操作之前的值
     */
    private String beforeValue;

    /**
     * 修改之后的值
     */
    private String afterValue;

    /**
     * 创建时间
     */
    private Date createTime;


}
