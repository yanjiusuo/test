package com.jd.workflow.console.entity.local;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * <p>
 * 本地测试记录创建需求空间
 * </p>
 *
 * @author sunchao81
 * @since 2024-07-02
 */
@Data
@Accessors(chain = true)
public class LocalRequirementParam {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "代码地址")
    @TableField("git_url")
    private String gitUrl;

    @ApiModelProperty(value = "代码分支")
    @TableField("git_branch")
    private String gitBranch;

    /**
     *
     */
    private String erp;

}
