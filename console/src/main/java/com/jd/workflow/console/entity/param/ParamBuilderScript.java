package com.jd.workflow.console.entity.param;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 入参构建脚本
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("param_builder_script")
@ApiModel(value="ParamBuilderScript对象", description="入参构建脚本")
public class ParamBuilderScript implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "场景名称")
    @TableField("script_name")
    private String scriptName;

    @ApiModelProperty(value = "脚本内容")
    @TableField("script_content")
    private String scriptContent;

    @ApiModelProperty(value = "脚本来源 1、物料平台无返回值  2、物料平台有返回值")
    @TableField("script_source")
    private Integer scriptSource;

    @ApiModelProperty(value = "逻辑删除标示 0、删除 1、有效")
    @TableField("yn")
    private Integer yn;

    @ApiModelProperty(value = "顺序")
    @TableField("i_sort")
    private Long iSort;

    @ApiModelProperty(value = "备注")
    @TableField("notes")
    private String notes;

    @ApiModelProperty(value = "创建人")
    @TableField("creator")
    private String creator;

    @ApiModelProperty(value = "修改者")
    @TableField("modifier")
    private String modifier;

    @ApiModelProperty(value = "创建时间")
    @TableField("created")
    private Date created;

    @ApiModelProperty(value = "修改时间")
    @TableField("modified")
    private Date modified;

    @ApiModelProperty(value = "租户id")
    @TableField("tenant_id")
    private Long tenantId;

    @ApiModelProperty(value = "应用id")
    @TableField("app_id")
    private Long appId;

    @ApiModelProperty(value = "类型：1.动态物料 2.静态物料")
    @TableField("type")
    private Integer type;

    @ApiModelProperty(value = "出参")
    @TableField("script_result")
    private String scriptResult;
}
