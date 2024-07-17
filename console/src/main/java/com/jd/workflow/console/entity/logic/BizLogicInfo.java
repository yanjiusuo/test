package com.jd.workflow.console.entity.logic;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * <p>
 * 组件业务逻辑信息表 从藏经阁同步
 * </p>
 *
 * @author zhaojingchun
 * @since 2024-06-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("biz_logic_info")
public class BizLogicInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接口名称
     */
    @TableField("interface_name")
    private String interfaceName;

    /**
     * 方法名称
     */
    @TableField("method_name")
    private String methodName;

    /**
     * 组件编号
     */
    @TableField("component_id")
    private Long componentId;

    /**
     *  1-接口文档 2-调用示例 3-业务逻辑
     */
    @TableField("type")
    private Integer type;

    /**
     * 创建时间
     */
    @TableField("created")
    private Date created;


    /**
     * 创建时间
     */
    @TableField("modified")
    private Date modified;

    /**
     * 逻辑删除标示 0、删除 1、有效
     */
    @TableField("yn")
    private Integer yn;


}