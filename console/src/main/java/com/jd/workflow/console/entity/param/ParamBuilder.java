package com.jd.workflow.console.entity.param;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
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
 * 入参构建器
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("param_builder")
@ApiModel(value="ParamBuilder对象", description="入参构建器")
public class ParamBuilder implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "场景名称")
    @TableField("scene_name")
    private String sceneName;

    @ApiModelProperty(value = "入参")
    @TableField("param_json")
    private String paramJson;

    @ApiModelProperty(value = "方法ID")
    @TableField("method_manage_id")
    private Long methodManageId;

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


}
