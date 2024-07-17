package com.jd.workflow.console.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <p>
 * 接口管理
 * </p>
 *
 * @author wubaizhao1
 * @since 2022-05-11
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "color_gateway_param", autoResultMap = true)
public class ColorGatewayParam extends BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 接口名称
     */
    private String name;
    /**
     * 方法id
     */
    private String methodId;
    /**
     * colorAPi
     */
    private String api;
    /**
     * 环境 pre、pro
     */
    private String zone;


    /**
     * 接口描述
     */
    private String description;

    /**
     * 参数类型   1-requestHeader 2-requestParam 3-responseHeader'
     */
    private Integer type;

    /**
     * 1-网关规范 3-http协议 2 用户自定义
     */
    private Integer mark;
    /**
     * 1-传递 0-否
     */

    private Integer isTransparent;
    /**
     * 1-必填 0-否
     */
    private Integer isAppNecessary;
    /**
     *1-网关生成 2-客户端传递
     */
    private Integer source;
    /**
     *1-可编辑 0-不可编辑
     */
    private Integer isEdit;
    /**
     * 字段类型 string  integer
     */
    private String dataType;
    /**
     * 默认是否展示 1=展示
     */
    @TableField(exist = false)
    private Integer defaultShow;

}
