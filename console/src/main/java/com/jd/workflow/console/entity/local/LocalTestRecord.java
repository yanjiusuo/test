package com.jd.workflow.console.entity.local;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 本地测试记录流水
 * </p>
 *
 * @author sunchao81
 * @since 2024-07-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("local_test_record")
@ApiModel(value="LocalTestRecord对象", description="本地测试记录流水")
public class LocalTestRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "用例ID")
    @TableField("requirement_info_id")
    private Long requirementInfoId;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "代码地址")
    @TableField("git_url")
    private String gitUrl;

    @ApiModelProperty(value = "代码分支")
    @TableField("git_branch")
    private String gitBranch;

    @ApiModelProperty(value = "接口名称")
    @TableField("interface_name")
    private String interfaceName;

    @ApiModelProperty(value = "方法名称")
    @TableField("method_name")
    private String methodName;

    @ApiModelProperty(value = "用例ID")
    @TableField("case_id")
    private Long caseId;

    @ApiModelProperty(value = "用例来源 1、jApi 2、deepTest 3、idea插件")
    @TableField("case_source")
    private Integer caseSource;

    @ApiModelProperty(value = "逻辑删除标示 0、删除 1、有效")
    @TableField("yn")
    private Integer yn;

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
