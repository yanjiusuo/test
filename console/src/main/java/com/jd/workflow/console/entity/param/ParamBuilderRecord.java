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
 * 入参构建器记录
 * </p>
 *
 * @author sunchao81
 * @since 2024-05-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("param_builder_record")
@ApiModel(value="ParamBuilderRecord对象", description="入参构建器记录")
public class ParamBuilderRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "构建后入参")
    @TableField("result_json")
    private String resultJson;

    @ApiModelProperty(value = "构建器ID")
    @TableField("param_builder_id")
    private Long paramBuilderId;

    @ApiModelProperty(value = "方法ID")
    @TableField("method_manage_id")
    private Long methodManageId;

    @ApiModelProperty(value = "执行信息")
    @TableField("run_msg")
    private String runMsg;

    @ApiModelProperty(value = "执行状态  0 初始化 1 待执行 2 执行中 3 执行成功 4 执行失败 ")
    @TableField("run_status")
    private Integer runStatus;

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

    @ApiModelProperty(value = "调用记录id")
    @TableField("debug_log_id")
    private Long debugLogId;

    @ApiModelProperty(value = "执行状态描述")
    @TableField(exist = false)
    private String runStatusDesc;

    @ApiModelProperty(value = "用例名称")
    @TableField(exist = false)
    private String sceneName;

    @ApiModelProperty(value = "标红标记：为true则标红")
    @TableField(exist = false)
    private Boolean redFlag = Boolean.FALSE;

    @ApiModelProperty(value = "断言总数")
    @TableField(exist = false)
    private Integer assertTotalNum;

    @ApiModelProperty(value = "断言成功数")
    @TableField(exist = false)
    private Integer assertSuccessNum;

    @ApiModelProperty(value = "断言失败数")
    @TableField(exist = false)
    private Integer assertFailNum;

}
